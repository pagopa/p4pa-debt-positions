package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary.OrdinaryInstallmentPaymentHandlerService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentFlowOrchestratorService {

  private final ReceiptService receiptService;
  private final DebtPositionService debtPositionService;
  private final OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerService;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;

  public PaymentFlowOrchestratorService(ReceiptService receiptService, DebtPositionService debtPositionService, OrdinaryInstallmentPaymentHandlerService ordinaryInstallmentPaymentHandlerService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService) {
    this.receiptService = receiptService;
    this.debtPositionService = debtPositionService;
    this.ordinaryInstallmentPaymentHandlerService = ordinaryInstallmentPaymentHandlerService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.unknownDebtPositionTypeOrgRetrieverService = unknownDebtPositionTypeOrgRetrieverService;
  }

  public void handleAlreadyPaidLogic(InstallmentNoPII installment,
                                     ReceiptWithAdditionalNodeDataDTO incomingReceiptDTO,
                                     DebtPosition dp,
                                     Runnable fullUpdateAction,
                                     Runnable syncWorkflowAction,
                                     String accessToken) {

    ReceiptDTO storedReceipt = receiptService.getReceipt(installment.getReceiptId());

    boolean isStoredPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(storedReceipt.getReceiptOrigin());
    boolean isIncomingPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(incomingReceiptDTO.getReceiptOrigin());

    if (isStoredPagoPa) {
      if (!isIncomingPagoPa) {
        log.info("Updating balance/dpTypeOrgId for debtPositionId {} (Stored: {}, Incoming: {})", dp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());
        updateBalanceAndMeta(dp, installment, incomingReceiptDTO.getDebtPositionTypeOrgCode(), accessToken);

        syncWorkflowAction.run();
      } else {
        log.info("Skipping update and workflow for DP {} (Both RECEIPT_PAGOPA origin)", dp.getDebtPositionId());
      }
    } else {
      if (isIncomingPagoPa) {
        log.info("Executing Full Update for DP {} (Stored: {}, Incoming: {})", dp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());

        fullUpdateAction.run();
        syncWorkflowAction.run();
      } else {
        log.info("Updating balance/dpTypeOrgId for DP {} (Both not RECEIPT_PAGOPA origin)", dp.getDebtPositionId());
        updateBalanceAndMeta(dp, installment, incomingReceiptDTO.getDebtPositionTypeOrgCode(), accessToken);

        syncWorkflowAction.run();
      }
    }
  }

  public void updateBalanceAndMeta(DebtPosition dp, InstallmentNoPII installment, String debtPositionTypeOrgCode, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findByOrganizationIdAndCode(dp.getOrganizationId(), debtPositionTypeOrgCode)
      .orElseGet(() -> unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(dp.getOrganizationId()));
    if (dp.getDebtPositionTypeOrgId() == -1) {
      log.info("DebtPosition {} has UNKNOWN debtPositionTypeOrgId. Updating to [{}] and saving to DB.", dp.getDebtPositionId(), debtPositionTypeOrg.getDebtPositionTypeOrgId());
      dp.setDebtPositionTypeOrgId(debtPositionTypeOrg.getDebtPositionTypeOrgId());
      debtPositionService.saveDebtPosition(dp);
    }
    log.info("Updating balance for installmentId[{}]", installment.getInstallmentId());
    ordinaryInstallmentPaymentHandlerService.resolveBalance(installment, accessToken);
  }
}
