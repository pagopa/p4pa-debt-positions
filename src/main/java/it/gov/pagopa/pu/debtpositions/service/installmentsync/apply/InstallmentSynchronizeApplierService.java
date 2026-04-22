package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Service
public class InstallmentSynchronizeApplierService {

  private final InstallmentSynchronizeMapper installmentSynchronizeMapper;
  private final InstallmentSynchronizeDebtPositionApplierService applierDebtPositionService;
  private final InstallmentSynchronizeInstallmentApplierService applierInstallmentService;
  private final InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final OrganizationService organizationService;
  private final CategoryResolverService categoryResolverService;

  public InstallmentSynchronizeApplierService(InstallmentSynchronizeMapper installmentSynchronizeMapper, InstallmentSynchronizeDebtPositionApplierService applierDebtPositionService, InstallmentSynchronizeInstallmentApplierService applierInstallmentService, InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, OrganizationService organizationService, CategoryResolverService categoryResolverService) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.applierDebtPositionService = applierDebtPositionService;
    this.applierInstallmentService = applierInstallmentService;
    this.applierPaymentOptionService = applierPaymentOptionService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.organizationService = organizationService;
    this.categoryResolverService = categoryResolverService;
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
        .orElseThrow(() -> new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_ORGANIZATION, String.format("Provided organization with id %s not found", organizationId)));

      populateFirstTransfer(installmentSynchronizeDTO, organization, debtPositionTypeOrg);
      applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO);
    }
    return installmentDTO;
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION_TYPE_ORG_CODE, String.format("The debt position type org with code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

  private void populateFirstTransfer(InstallmentSynchronizeDTO installmentSynchronizeDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg) {
    if (installmentSynchronizeDTO.getAdditionalTransfers().stream()
      .anyMatch(transferDTO -> transferDTO.getTransferIndex() == 1)) {
      return;
    }

    String category = categoryResolverService.resolveCategory(installmentSynchronizeDTO.getLegacyPaymentMetadata(), debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode());

    Long totalAmountOtherTransfers = installmentSynchronizeDTO.getAdditionalTransfers().stream()
      .mapToLong(TransferSynchronizeDTO::getAmountCents).sum();

    Pair<String, String> resolvedIbanAndPostalIban = Utilities.resolveIbanAndPostalIban(
      debtPositionTypeOrg.getIban(),
      debtPositionTypeOrg.getPostalIban(),
      organization.getIban(),
      organization.getPostalIban()
    );

    TransferSynchronizeDTO firstTransfer = TransferSynchronizeDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(resolvedIbanAndPostalIban.getLeft())
      .postalIban(resolvedIbanAndPostalIban.getRight())
      .category(category)
      .amountCents(installmentSynchronizeDTO.getAmountCents() - totalAmountOtherTransfers)
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .build();

    installmentSynchronizeDTO.addAdditionalTransfersItem(firstTransfer);
  }

}
