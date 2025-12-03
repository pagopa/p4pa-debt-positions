package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.StandardPaymentUpdateService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.PaymentFlowOrchestratorService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.springframework.stereotype.Service;

@Service
public class ReceiptBasedTechnicalDpHandlerService {

  private final ReceiptWithAdditionalInfoMapper receiptMapper;
  private final DebtPositionService debtPositionService;
  private final PaymentsProducerService paymentsProducerService;
  private final DebtPositionMapper debtPositionMapper;
  private final TechnicalDpUpdateService technicalDpUpdateService;
  private final StandardPaymentUpdateService standardPaymentUpdateService;
  private final PaymentFlowOrchestratorService paymentFlowOrchestratorService;
  private final ReceiptService receiptService;

  public ReceiptBasedTechnicalDpHandlerService(ReceiptWithAdditionalInfoMapper receiptMapper,
                                               DebtPositionService debtPositionService,
                                               PaymentsProducerService paymentsProducerService,
                                               DebtPositionMapper debtPositionMapper,
                                               TechnicalDpUpdateService technicalDpUpdateService,
                                               StandardPaymentUpdateService standardPaymentUpdateService,
                                               PaymentFlowOrchestratorService paymentFlowOrchestratorService,
                                               ReceiptService receiptService) {
    this.receiptMapper = receiptMapper;
    this.debtPositionService = debtPositionService;
    this.paymentsProducerService = paymentsProducerService;
    this.debtPositionMapper = debtPositionMapper;
    this.technicalDpUpdateService = technicalDpUpdateService;
    this.standardPaymentUpdateService = standardPaymentUpdateService;
    this.paymentFlowOrchestratorService = paymentFlowOrchestratorService;
    this.receiptService = receiptService;
  }

  public DebtPosition createAndPublishTechDp(Organization organization, ReceiptWithAdditionalNodeDataDTO receiptDTO){
    DebtPositionDTO debtPositionDTO = receiptMapper.mapToDebtPosition(receiptDTO, organization);
    debtPositionService.saveDebtPosition(debtPositionDTO);
    return publishTechDp(debtPositionDTO, receiptDTO);
  }

  public DebtPosition publishTechDp(DebtPositionDTO debtPositionDTO, ReceiptWithAdditionalNodeDataDTO receiptDTO){
    paymentsProducerService.notifyPaymentsEvent(debtPositionDTO, PaymentEventType.RT_RECEIVED, "receiptId:" + receiptDTO.getReceiptId());
    return debtPositionMapper.mapToModel(debtPositionDTO);
  }

  public DebtPosition updateAndPublishTechDp(Organization organization, DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    Runnable syncPublishAction = () -> publishTechDp(debtPositionMapper.mapToDto(dp), receiptDTO);

    Runnable fullUpdateAction = () ->
      standardPaymentUpdateService.performStandardUpdate(dp, installment, receiptDTO, accessToken);

    Runnable partialUpdateAction = () -> {
      ReceiptDTO storedReceipt = receiptService.getReceipt(installment.getReceiptId());

      if (ReceiptOriginType.RECEIPT_PAGOPA.equals(storedReceipt.getReceiptOrigin())) {
        paymentFlowOrchestratorService.updateBalanceAndMeta(dp, installment, receiptDTO.getDebtPositionTypeOrgCode(), accessToken);
      } else {
        technicalDpUpdateService.updateDp(dp, receiptDTO, organization);
      }
    };

    paymentFlowOrchestratorService.handleAlreadyPaidLogic(
      installment,
      receiptDTO,
      dp,
      fullUpdateAction,
      partialUpdateAction,
      syncPublishAction
    );

    return dp;
  }
}
