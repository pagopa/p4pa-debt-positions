package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.common.IllegalStateBusinessException;
import it.gov.pagopa.pu.debtpositions.exception.common.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper.InstallmentSynchronizeMapper;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import it.gov.pagopa.pu.organization.dto.generated.PagoPaInteractionModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
      String stationId = validateIupdPagopaAndRetrieveStationId(installmentSynchronizeDTO, accessToken);
      DebtPositionDTO debtPositionDTO = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg, stationId);
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
      InstallmentDTO installmentDTO = applyInstallment(installmentSynchronizeDTO, storedPaymentOption, storedInstallment, accessToken, debtPositionTypeOrg, storedDebtPosition.getDebtPositionOrigin());
      return Pair.of(storedDebtPosition, installmentDTO);
    }
  }

  private InstallmentDTO applyInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, PaymentOptionDTO storedPaymentOption, InstallmentDTO installmentDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionOrigin debtPositionOrigin) {
    if (installmentDTO == null) {
      installmentDTO = installmentSynchronizeMapper.map2Installment(installmentSynchronizeDTO);
      storedPaymentOption.getInstallments().add(installmentDTO);
    } else {
      Long organizationId = installmentSynchronizeDTO.getOrganizationId();
      Organization organization = organizationService.getOrganizationById(organizationId, accessToken)
        .orElseThrow(() -> new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_ORGANIZATION, String.format("Provided organization with id %s not found", organizationId)));

      populateFirstTransfer(installmentSynchronizeDTO, organization, debtPositionTypeOrg, debtPositionOrigin);
      applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO);
    }
    return installmentDTO;
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_DEBT_POSITION_TYPE_ORG_CODE, String.format("The debt position type org with code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

  private void populateFirstTransfer(InstallmentSynchronizeDTO installmentSynchronizeDTO, Organization organization, DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionOrigin debtPositionOrigin) {
    if (installmentSynchronizeDTO.getAdditionalTransfers().stream()
      .anyMatch(transferDTO -> transferDTO.getTransferIndex() == 1)) {
      return;
    }

    String category = categoryResolverService.resolveCategory(installmentSynchronizeDTO.getLegacyPaymentMetadata(), debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode(), debtPositionOrigin);

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

  private String validateIupdPagopaAndRetrieveStationId(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken) {
    Long orgId = installmentSynchronizeDTO.getOrganizationId();

    OrganizationStationDTO organizationStationDTO = organizationService
      .getOrganizationStation(orgId, null, accessToken)
      .orElseThrow(() -> new IllegalStateBusinessException(
        ErrorCodeConstants.ERROR_CODE_ORGANIZATION_STATION_NOT_FOUND,
        String.format("Station for org with id %s not found", orgId)
      ));

    Boolean flagPuPagoPaPayment = installmentSynchronizeDTO.getFlagPuPagoPaPayment();
    String iupdPagopa = installmentSynchronizeDTO.getIupdPagopa();

    boolean isGpd = Objects.equals(organizationStationDTO.getPagoPaInteractionModel(), PagoPaInteractionModel.ASYNC_GPD);

    if (Boolean.FALSE.equals(flagPuPagoPaPayment) && isGpd && StringUtils.isBlank(iupdPagopa)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_MISSING_IUPD_PAGOPA, "iupdPagopa is mandatory when flagPuPagoPaPayment is false and is a gpd station");
    }

    if (Boolean.TRUE.equals(flagPuPagoPaPayment) && StringUtils.isNotBlank(iupdPagopa)) {
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_IUPD_PAGOPA, "iupdPagopa must not be populated when flagPuPagoPaPayment is true");
    }

    return organizationStationDTO.getStationId();
  }
}
