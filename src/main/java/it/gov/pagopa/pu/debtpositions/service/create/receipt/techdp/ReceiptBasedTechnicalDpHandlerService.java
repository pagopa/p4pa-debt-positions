package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.stereotype.Service;

@Service
public class ReceiptBasedTechnicalDpHandlerService {

  private final ReceiptWithAdditionalInfoMapper receiptMapper;
  private final DebtPositionService debtPositionService;
  private final UpdateAndSynchronizeTechDp updateAndSynchronizeTechDp;

  public ReceiptBasedTechnicalDpHandlerService(ReceiptWithAdditionalInfoMapper receiptMapper, DebtPositionService debtPositionService, UpdateAndSynchronizeTechDp updateAndSynchronizeTechDp) {
    this.receiptMapper = receiptMapper;
    this.debtPositionService = debtPositionService;
    this.updateAndSynchronizeTechDp = updateAndSynchronizeTechDp;
  }

  public DebtPosition createAndPublishTechDp(Organization organization, ReceiptWithAdditionalNodeDataDTO receiptDTO){
    DebtPositionDTO debtPositionDTO = receiptMapper.mapToDebtPosition(receiptDTO, organization);
    debtPositionService.saveDebtPosition(debtPositionDTO);
    return updateAndSynchronizeTechDp.publishTechDp(debtPositionDTO, receiptDTO);
  }

  public DebtPosition updateAndPublishTechDp(Organization organization, DebtPosition dp, InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    return updateAndSynchronizeTechDp.handleTechDpAlreadyPaid(
      installment,
      receiptDTO,
      dp,
      organization,
      accessToken
    );
  }
}
