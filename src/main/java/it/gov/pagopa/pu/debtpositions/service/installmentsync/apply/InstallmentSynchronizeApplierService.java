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
import org.springframework.stereotype.Service;

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

  public InstallmentDTO apply(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO debtPositionDTO,
                              PaymentOptionDTO paymentOptionDTO, InstallmentDTO installmentDTO, String accessToken) {
    Long organizationId = installmentSynchronizeDTO.getOrganizationId();
    Organization organization = organizationService.getOrganizationById(organizationId, accessToken)
      .orElseThrow(() -> new InvalidValueException(String.format("Provided organization id %s not found", organizationId)));

    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(organizationId,
      installmentSynchronizeDTO.getDebtPositionTypeCode());
    populateFirstTransfer(installmentSynchronizeDTO, organization, debtPositionTypeOrg);

    if (debtPositionDTO == null) {
      return installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId())
        .getPaymentOptions().getFirst()
        .getInstallments().getFirst();
    }
    applierDebtPositionService.merge(installmentSynchronizeDTO, debtPositionDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());

    return applyPaymentOption(installmentSynchronizeDTO, debtPositionDTO, paymentOptionDTO, installmentDTO);
  }

  private InstallmentDTO applyPaymentOption(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO debtPositionDTO, PaymentOptionDTO paymentOptionDTO, InstallmentDTO installmentDTO) {
    if (paymentOptionDTO == null) {
      paymentOptionDTO = installmentSynchronizeMapper.map2PaymentOptionDTO(installmentSynchronizeDTO);
      debtPositionDTO.getPaymentOptions().add(paymentOptionDTO);
      return paymentOptionDTO.getInstallments().getFirst();
    } else {
      applierPaymentOptionService.merge(installmentSynchronizeDTO, paymentOptionDTO);
      return applyInstallment(installmentSynchronizeDTO, paymentOptionDTO, installmentDTO);
    }
  }

  private InstallmentDTO applyInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, PaymentOptionDTO paymentOptionDTO, InstallmentDTO installmentDTO) {
    if (installmentDTO == null) {
      installmentDTO = installmentSynchronizeMapper.map2Installment(installmentSynchronizeDTO);
      paymentOptionDTO.getInstallments().add(installmentDTO);
    } else {
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
    String category = debtPositionTypeRepository.findById(debtPositionTypeOrg.getDebtPositionTypeId())
      .orElseThrow(() -> new NotFoundException(String.format("The debt position type with id %s is not found", debtPositionTypeOrg.getDebtPositionTypeId())))
      .getTaxonomyCode();

    Long totalAmountOtherTransfers = installmentSynchronizeDTO.getAdditionalTransfers().stream()
      .mapToLong(TransferSynchronizeDTO::getAmountCents).sum();

    TransferSynchronizeDTO firstTransfer = TransferSynchronizeDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban(debtPositionTypeOrg.getIban().isBlank() ? organization.getIban() : debtPositionTypeOrg.getIban())
      .category(category)
      .amountCents(installmentSynchronizeDTO.getAmountCents() - totalAmountOtherTransfers)
      .remittanceInformation(installmentSynchronizeDTO.getRemittanceInformation())
      .build();

    installmentSynchronizeDTO.addAdditionalTransfersItem(firstTransfer);
  }
}
