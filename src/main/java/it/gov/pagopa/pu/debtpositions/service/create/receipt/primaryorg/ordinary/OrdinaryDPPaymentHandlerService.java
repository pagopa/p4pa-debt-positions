package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrdinaryDPPaymentHandlerService {

  private final StandardPaymentUpdateService standardPaymentUpdateService;
  private final DebtPositionMapper mapper;
  private final DebtPositionSyncService syncService;
  private final PaymentFlowOrchestratorService paymentFlowOrchestratorService;

  public OrdinaryDPPaymentHandlerService(StandardPaymentUpdateService standardPaymentUpdateService,
                                         DebtPositionMapper mapper,
                                         DebtPositionSyncService syncService,
                                         PaymentFlowOrchestratorService paymentFlowOrchestratorService) {
    this.standardPaymentUpdateService = standardPaymentUpdateService;
    this.mapper = mapper;
    this.syncService = syncService;
    this.paymentFlowOrchestratorService = paymentFlowOrchestratorService;
  }

  public void handlePayment(DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    Runnable syncWorkflowAction = () -> invokeWorkflow(dp, receiptDTO, accessToken);

    if (!InstallmentUtils.PAID_STATUSES.contains(installment.getStatus())) {
      standardPaymentUpdateService.performStandardUpdate(dp, installment, receiptDTO, accessToken);
      syncWorkflowAction.run();
    } else {
      paymentFlowOrchestratorService.handleAlreadyPaidLogic(
        installment,
        receiptDTO,
        dp,
        () -> standardPaymentUpdateService.performStandardUpdate(dp, installment, receiptDTO, accessToken),
        () -> paymentFlowOrchestratorService.updateBalanceAndMeta(dp, installment, receiptDTO, accessToken),
        syncWorkflowAction
      );
    }
  }

  private void invokeWorkflow(DebtPosition dp, ReceiptDTO receiptDTO, String accessToken) {
    DebtPositionDTO dpDTO = mapper.mapToDto(dp);
    log.info("Synchronizing DebtPosition {}", dpDTO.getDebtPositionId());
    syncService.syncDebtPosition(dpDTO, new WfExecutionParameters(), PaymentEventType.RT_RECEIVED, "receiptId:" + receiptDTO.getReceiptId(), accessToken);
  }
}
