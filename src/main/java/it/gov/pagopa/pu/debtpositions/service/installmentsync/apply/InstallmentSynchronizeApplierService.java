package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.LEGACY_PAYMENT_METADATA_REGEX;
import static it.gov.pagopa.pu.debtpositions.util.Utilities.taxonomyCodeToTransferCategory;

@Service
public class InstallmentSynchronizeApplierService {

  private final InstallmentSynchronizeMapper installmentSynchronizeMapper;
  private final InstallmentSynchronizeDebtPositionApplierService applierDebtPositionService;
  private final InstallmentSynchronizeInstallmentApplierService applierInstallmentService;
  private final InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final OrganizationService organizationService;
  private final DebtPositionTypeRepository debtPositionTypeRepository;

  public InstallmentSynchronizeApplierService(InstallmentSynchronizeMapper installmentSynchronizeMapper, InstallmentSynchronizeDebtPositionApplierService applierDebtPositionService, InstallmentSynchronizeInstallmentApplierService applierInstallmentService, InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, OrganizationService organizationService, DebtPositionTypeRepository debtPositionTypeRepository) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.applierDebtPositionService = applierDebtPositionService;
    this.applierInstallmentService = applierInstallmentService;
    this.applierPaymentOptionService = applierPaymentOptionService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.organizationService = organizationService;
    this.debtPositionTypeRepository = debtPositionTypeRepository;
  }

  public Pair<DebtPositionDTO, InstallmentDTO> apply(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO storedDebtPosition,
                                                     PaymentOptionDTO storedPaymentOption, InstallmentDTO storedInstallment, String accessToken) {

    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(installmentSynchronizeDTO.getOrganizationId(),
      installmentSynchronizeDTO.getDebtPositionTypeCode());

    if (storedDebtPosition == null) {
      DebtPositionDTO debtPositionDTO = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg);
      return Pair.of(debtPositionDTO, debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    }
    applierDebtPositionService.merge(installmentSynchronizeDTO, storedDebtPosition, debtPositionTypeOrg.getDebtPositionTypeOrgId());

    return applyPaymentOption(installmentSynchronizeDTO, storedDebtPosition, storedPaymentOption, storedInstallment, accessToken, debtPositionTypeOrg);
  }

  private Pair<DebtPositionDTO, InstallmentDTO> applyPaymentOption(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO storedDebtPosition, PaymentOptionDTO storedPaymentOption, InstallmentDTO storedInstallment, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {
    if (storedPaymentOption == null) {
      PaymentOptionDTO paymentOptionDTO = installmentSynchronizeMapper.map2PaymentOptionDTO(installmentSynchronizeDTO);
      storedDebtPosition.addPaymentOptionsItem(paymentOptionDTO);
      return Pair.of(storedDebtPosition, paymentOptionDTO.getInstallments().getFirst());
    } else {
      applierPaymentOptionService.merge(installmentSynchronizeDTO, storedPaymentOption);
      InstallmentDTO installmentDTO = applyInstallment(installmentSynchronizeDTO, storedPaymentOption, storedInstallment, accessToken, debtPositionTypeOrg);
      return Pair.of(storedDebtPosition, installmentDTO);
    }
  }

  private InstallmentDTO applyInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, PaymentOptionDTO storedPaymentOption, InstallmentDTO installmentDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg) {
    if (installmentDTO == null) {
      installmentDTO = installmentSynchronizeMapper.map2Installment(installmentSynchronizeDTO);
      storedPaymentOption.getInstallments().add(installmentDTO);
    } else {
      Long organizationId = installmentSynchronizeDTO.getOrganizationId();
      Organization organization = organizationService.getOrganizationById(organizationId, accessToken)
        .orElseThrow(() -> new InvalidValueException(String.format("Provided organization id %s not found", organizationId)));

      populateFirstTransfer(installmentSynchronizeDTO, organization, debtPositionTypeOrg);
      applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO);
    }
    return installmentDTO;
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(String.format("The debt position type code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

  private void populateFirstTransfer(InstallmentSynchronizeDTO installmentSynchronizeDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg) {
    String taxonomyCode = Optional.ofNullable(installmentSynchronizeDTO.getLegacyPaymentMetadata())
      .filter(StringUtils::isNotBlank)
      .map(this::extractTaxonomyFromLegacyPaymentMetadata)
      .orElseGet(() -> getTaxonomyFromRepository(debtPositionTypeOrg.getDebtPositionTypeId()));

    Long totalAmountOtherTransfers = installmentSynchronizeDTO.getAdditionalTransfers().stream()
      .mapToLong(TransferSynchronizeDTO::getAmountCents).sum();

    TransferSynchronizeDTO firstTransfer = TransferSynchronizeDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(StringUtils.isEmpty(debtPositionTypeOrg.getIban()) ? organization.getIban() : debtPositionTypeOrg.getIban())
      .category(taxonomyCodeToTransferCategory(taxonomyCode))
      .amountCents(installmentSynchronizeDTO.getAmountCents() - totalAmountOtherTransfers)
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .build();

    installmentSynchronizeDTO.addAdditionalTransfersItem(firstTransfer);
  }

  private String extractTaxonomyFromLegacyPaymentMetadata(String legacyPaymentMetadata) {
    Matcher matcher = LEGACY_PAYMENT_METADATA_REGEX.matcher(legacyPaymentMetadata);
    if (!matcher.find()) {
      throw new InvalidValueException(String.format("The legacy payment metadata [%s] does not valid to extract taxonomy code", legacyPaymentMetadata));
    }
    return matcher.group(1);
  }

  private String getTaxonomyFromRepository(Long debtPositionTypeId) {
    return debtPositionTypeRepository.findById(debtPositionTypeId)
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type with id %s is not found", debtPositionTypeId)))
      .getTaxonomyCode();
  }
}
