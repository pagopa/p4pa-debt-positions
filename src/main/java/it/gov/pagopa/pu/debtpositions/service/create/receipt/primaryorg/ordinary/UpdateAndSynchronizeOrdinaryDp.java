package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.utils.PaymentFlowOrchestratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UpdateAndSynchronizeOrdinaryDp {

  private final ReceiptService receiptService;
  private final PaymentFlowOrchestratorService paymentFlowOrchestratorService;

  public UpdateAndSynchronizeOrdinaryDp(ReceiptService receiptService, PaymentFlowOrchestratorService paymentFlowOrchestratorService) {
    this.receiptService = receiptService;
    this.paymentFlowOrchestratorService = paymentFlowOrchestratorService;
  }

  public void handleOrdinaryDpAlreadyPaid(InstallmentNoPII installment,
                                          ReceiptWithAdditionalNodeDataDTO incomingReceiptDTO,
                                          DebtPosition dp,
                                          Runnable syncWorkflowAction,
                                          String accessToken) {

    ReceiptDTO storedReceipt = receiptService.getReceipt(installment.getReceiptId());

    boolean isStoredPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(storedReceipt.getReceiptOrigin());
    boolean isIncomingPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(incomingReceiptDTO.getReceiptOrigin());

    if (isStoredPagoPa) {
      if (!isIncomingPagoPa) {
        log.info("Updating balance/dpTypeOrgId for debtPositionId {} (Stored: {}, Incoming: {})", dp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());
        paymentFlowOrchestratorService.updateBalanceAndMeta(dp, installment, incomingReceiptDTO, accessToken);

        syncWorkflowAction.run();
      } else {
        log.info("Skipping update and workflow for DP {} (Both RECEIPT_PAGOPA origin)", dp.getDebtPositionId());
      }
    } else {
      if (isIncomingPagoPa) {
        log.info("Executing Full Update for DP {} (Stored: {}, Incoming: {})", dp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());

        paymentFlowOrchestratorService.performStandardUpdate(dp, installment, incomingReceiptDTO, accessToken);
        syncWorkflowAction.run();
      } else {
        log.info("Updating balance/dpTypeOrgId for DP {} (Both not RECEIPT_PAGOPA origin)", dp.getDebtPositionId());
        paymentFlowOrchestratorService.updateBalanceAndMeta(dp, installment, incomingReceiptDTO, accessToken);

        syncWorkflowAction.run();
      }
    }
  }
}
