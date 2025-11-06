package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.event.producer.PaymentsProducerService;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
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

  public ReceiptBasedTechnicalDpHandlerService(ReceiptWithAdditionalInfoMapper receiptMapper, DebtPositionService debtPositionService, PaymentsProducerService paymentsProducerService, DebtPositionMapper debtPositionMapper, TechnicalDpUpdateService technicalDpUpdateService) {
    this.receiptMapper = receiptMapper;
    this.debtPositionService = debtPositionService;
    this.paymentsProducerService = paymentsProducerService;
    this.debtPositionMapper = debtPositionMapper;
    this.technicalDpUpdateService = technicalDpUpdateService;
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

  public DebtPosition updateAndPublishTechDp(Organization organization, DebtPosition dp, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    DebtPositionDTO debtPositionDTO = technicalDpUpdateService.updateDp(dp, receiptDTO, organization);
    return publishTechDp(debtPositionDTO, receiptDTO);
  }
}
