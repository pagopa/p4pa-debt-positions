package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.utils.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UpdateAndSynchronizeTechDp {

  private final ReceiptService receiptService;
  private final PaymentFlowOrchestratorService paymentFlowOrchestratorService;
  private final TechnicalDpUpdateService technicalDpUpdateService;
  private final PaymentsProducerService paymentsProducerService;
  private final DebtPositionMapper debtPositionMapper;

  public UpdateAndSynchronizeTechDp(ReceiptService receiptService, PaymentFlowOrchestratorService paymentFlowOrchestratorService, TechnicalDpUpdateService technicalDpUpdateService, PaymentsProducerService paymentsProducerService, DebtPositionMapper debtPositionMapper) {
    this.receiptService = receiptService;
    this.paymentFlowOrchestratorService = paymentFlowOrchestratorService;
    this.technicalDpUpdateService = technicalDpUpdateService;
    this.paymentsProducerService = paymentsProducerService;
    this.debtPositionMapper = debtPositionMapper;
  }

  public DebtPosition handleTechDpAlreadyPaid(InstallmentNoPII installment,
                                              ReceiptWithAdditionalNodeDataDTO incomingReceiptDTO,
                                              DebtPosition storedDp,
                                              Organization organization,
                                              String accessToken) {

    ReceiptDTO storedReceipt = receiptService.getReceipt(installment.getReceiptId());

    boolean isStoredPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(storedReceipt.getReceiptOrigin());
    boolean isIncomingPagoPa = ReceiptOriginType.RECEIPT_PAGOPA.equals(incomingReceiptDTO.getReceiptOrigin());

    if (isStoredPagoPa) {
      if (!isIncomingPagoPa) {
        log.info("Updating balance/dpTypeOrgId for debtPositionId {} (Receipt - [Stored: {}, Incoming: {}])", storedDp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());
        paymentFlowOrchestratorService.updateBalanceAndMeta(storedDp, installment, incomingReceiptDTO, accessToken);
        publishTechDp(debtPositionMapper.mapToDto(storedDp), incomingReceiptDTO);
      } else {
        log.info("Skipping update and workflow for DP {} (Both RECEIPT_PAGOPA origin)", storedDp.getDebtPositionId());
      }
    } else {
      if (isIncomingPagoPa) {
        log.info("Executing Full Update for DP {} (Receipt - [Stored: {}, Incoming: {}])", storedDp.getDebtPositionId(), storedReceipt.getReceiptOrigin(), incomingReceiptDTO.getReceiptOrigin());
        paymentFlowOrchestratorService.performStandardUpdate(storedDp, installment, incomingReceiptDTO, accessToken);
        publishTechDp(debtPositionMapper.mapToDto(storedDp), incomingReceiptDTO);
      } else {
        log.info("Updating DP {} (Both not RECEIPT_PAGOPA origin)", storedDp.getDebtPositionId());

        Long debtPositionTypeOrgId = paymentFlowOrchestratorService.resolveDebtPositionTypeOrgId(
          organization.getOrganizationId(), incomingReceiptDTO.getDebtPositionTypeOrgCode(), storedDp.getDebtPositionTypeOrgId());

        DebtPositionDTO dto = technicalDpUpdateService.updateDp(storedDp, incomingReceiptDTO, organization, debtPositionTypeOrgId);

        publishTechDp(dto, incomingReceiptDTO);
      }
    }
    return storedDp;
  }

  public DebtPosition publishTechDp(DebtPositionDTO debtPositionDTO, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    paymentsProducerService.notifyPaymentsEvent(debtPositionDTO, PaymentEventType.RT_RECEIVED, "receiptId:" + receiptDTO.getReceiptId());
    return debtPositionMapper.mapToModel(debtPositionDTO);
  }
}
