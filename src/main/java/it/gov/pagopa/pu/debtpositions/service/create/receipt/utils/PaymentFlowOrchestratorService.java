package it.gov.pagopa.pu.debtpositions.service.create.receipt.utils;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryInstallmentPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryPaidDPHierarchyUpdateService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PaymentFlowOrchestratorService {

    private final DebtPositionRepository debtPositionRepository;
    private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
    private final InstallmentNoPIIRepository installmentNoPIIRepository;
    private final OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerService;
    private final UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;
    private final OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerService;
    private final OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateService;
    private final DebtPositionService debtPositionService;

    public PaymentFlowOrchestratorService(DebtPositionRepository debtPositionRepository, InstallmentNoPIIRepository installmentNoPIIRepository, OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService, OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerService, OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateService, DebtPositionService debtPositionService) {
        this.debtPositionRepository = debtPositionRepository;
        this.installmentNoPIIRepository = installmentNoPIIRepository;
        this.ordinaryInstallmentPaymentHandlerService = ordinaryInstallmentPaymentHandlerService;
        this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
        this.unknownDebtPositionTypeOrgRetrieverService = unknownDebtPositionTypeOrgRetrieverService;
        this.installmentPaymentHandlerService = installmentPaymentHandlerService;
        this.hierarchyUpdateService = hierarchyUpdateService;
        this.debtPositionService = debtPositionService;
    }

    public void updateBalanceAndMeta(DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO incomingReceiptDTO, String accessToken) {
        DebtPositionTypeOrg unknownTypeOrg = unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(dp.getOrganizationId());
        DebtPositionTypeOrg typeOrgToUse = null;

        if (Objects.equals(dp.getDebtPositionTypeOrgId(), unknownTypeOrg.getDebtPositionTypeOrgId())) {
            Optional<DebtPositionTypeOrg> specificTypeOrgOpt = debtPositionTypeOrgRepository.findByOrganizationIdAndCode(dp.getOrganizationId(), incomingReceiptDTO.getDebtPositionTypeOrgCode());

            if (specificTypeOrgOpt.isPresent()) {
                DebtPositionTypeOrg specificTypeOrg = specificTypeOrgOpt.get();
                log.info("Updating DebtPosition {} from UNKNOWN to specific DebtPositionTypeOrgId [{}]", dp.getDebtPositionId(), specificTypeOrg.getDebtPositionTypeOrgId());

                debtPositionRepository.updateDebtPositionTypeOrgId(dp.getDebtPositionId(), specificTypeOrg.getDebtPositionTypeOrgId());
                dp.setDebtPositionTypeOrgId(specificTypeOrg.getDebtPositionTypeOrgId());
                typeOrgToUse = specificTypeOrg;
            } else {
                typeOrgToUse = unknownTypeOrg;
            }
        }

        log.info("Updating balance for installmentId[{}]", installment.getInstallmentId());
        installment.setBalance(incomingReceiptDTO.getBalance());

        if (typeOrgToUse != null) {
            ordinaryInstallmentPaymentHandlerService.resolveBalance(installment, typeOrgToUse, incomingReceiptDTO.getPaymentDateTime(), accessToken);
        } else {
            ordinaryInstallmentPaymentHandlerService.resolveBalance(installment, incomingReceiptDTO.getPaymentDateTime(), accessToken);
        }
        installmentNoPIIRepository.updateBalance(installment.getInstallmentId(), installment.getBalance());
    }

    public void performStandardUpdate(DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
        installmentPaymentHandlerService.updateInstallment(installment, receiptDTO, accessToken);
        hierarchyUpdateService.updateHierarchy(dp, installment);
        debtPositionService.saveDebtPosition(dp);
    }

  public Long resolveDebtPositionTypeOrgId(Long organizationId, String receiptDpTypeOrgCode, Long storedDpTypeOrgId) {
    DebtPositionTypeOrg unknownTypeOrg = unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(organizationId);

    if (storedDpTypeOrgId != null && !Objects.equals(storedDpTypeOrgId, unknownTypeOrg.getDebtPositionTypeOrgId())) {
      return storedDpTypeOrgId;
    }

    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(organizationId, receiptDpTypeOrgCode)
            .map(DebtPositionTypeOrg::getDebtPositionTypeOrgId)
            .orElse(unknownTypeOrg.getDebtPositionTypeOrgId());
  }
}

