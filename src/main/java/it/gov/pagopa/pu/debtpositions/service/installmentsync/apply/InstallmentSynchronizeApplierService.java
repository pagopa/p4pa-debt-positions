package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.mapper.InstallmentSynchronizeMapper;
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
  private final DebtPositionProcessorService debtPositionProcessorService;

  public InstallmentSynchronizeApplierService(InstallmentSynchronizeMapper installmentSynchronizeMapper, InstallmentSynchronizeDebtPositionApplierService applierDebtPositionService, InstallmentSynchronizeInstallmentApplierService applierInstallmentService, InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, OrganizationService organizationService, DebtPositionProcessorService debtPositionProcessorService) {
    this.installmentSynchronizeMapper = installmentSynchronizeMapper;
    this.applierDebtPositionService = applierDebtPositionService;
    this.applierInstallmentService = applierInstallmentService;
    this.applierPaymentOptionService = applierPaymentOptionService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.organizationService = organizationService;
    this.debtPositionProcessorService = debtPositionProcessorService;
  }

  public Pair<DebtPositionDTO, InstallmentDTO> apply(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO storedDebtPosition,
                                                     PaymentOptionDTO storedPaymentOption, InstallmentDTO storedInstallment, String accessToken) {

    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(installmentSynchronizeDTO.getOrganizationId(),
      installmentSynchronizeDTO.getDebtPositionTypeCode());

    if (storedDebtPosition == null) {
      DebtPositionDTO debtPositionDTO = installmentSynchronizeMapper.map2DebtPositionDTO(installmentSynchronizeDTO, debtPositionTypeOrg.getDebtPositionTypeOrgId());
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
      Organization organization = organizationService.getOrganizationById(installmentSynchronizeDTO.getOrganizationId(), accessToken)
        .orElseThrow(() -> new InvalidValueException(String.format("Provided organization id %s not found", installmentSynchronizeDTO.getOrganizationId())));

      debtPositionProcessorService.populateFirstTransfer(installmentDTO, organization, debtPositionTypeOrg);

      applierInstallmentService.merge(installmentSynchronizeDTO, installmentDTO);
    }
    return installmentDTO;
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(String.format("The debt position type code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

}
