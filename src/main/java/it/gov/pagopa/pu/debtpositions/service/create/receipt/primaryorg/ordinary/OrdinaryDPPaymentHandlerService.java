package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrdinaryDPPaymentHandlerService {

  private final OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerService;
  private final OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateService;
  private final DebtPositionService debtPositionService;
  private final DebtPositionMapper mapper;
  private final DebtPositionSyncService syncService;

  public OrdinaryDPPaymentHandlerService(OrdinaryInstallmentPaymentHandlerService installmentPaymentHandlerService, OrdinaryPaidDPHierarchyUpdateService hierarchyUpdateService, DebtPositionService debtPositionService, DebtPositionMapper mapper, DebtPositionSyncService syncService) {
    this.installmentPaymentHandlerService = installmentPaymentHandlerService;
    this.hierarchyUpdateService = hierarchyUpdateService;
    this.debtPositionService = debtPositionService;
    this.mapper = mapper;
    this.syncService = syncService;
  }

  public void handlePayment(DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    if (!InstallmentUtils.PAID_STATUSES.contains(installment.getStatus())) {
      installmentPaymentHandlerService.updateInstallment(installment, receiptDTO, accessToken);
      hierarchyUpdateService.updateHierarchy(dp, installment);
      debtPositionService.saveDebtPosition(dp);
    }

    invokeWorkflow(dp, receiptDTO, accessToken);
  }

  private void invokeWorkflow(DebtPosition dp, ReceiptDTO receiptDTO, String accessToken) {
    DebtPositionDTO dpDTO = mapper.mapToDto(dp);
    log.info("Synchronizing DebtPosition {}", dpDTO.getDebtPositionId());
    syncService.syncDebtPosition(dpDTO, new WfExecutionParameters(), PaymentEventType.RT_RECEIVED, "receiptId:" + receiptDTO.getReceiptId(), accessToken);
  }
}
