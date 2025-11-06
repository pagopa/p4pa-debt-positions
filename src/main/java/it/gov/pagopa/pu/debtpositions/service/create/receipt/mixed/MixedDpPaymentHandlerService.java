package it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.ReceiptBasedTechnicalDpHandlerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MixedDpPaymentHandlerService {

  private final TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterService;
  private final DebtPositionMapper mapper;
  private final ReceiptBasedTechnicalDpHandlerService technicalDpHandlerService;

  public MixedDpPaymentHandlerService(TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterService, DebtPositionMapper mapper, ReceiptBasedTechnicalDpHandlerService technicalDpHandlerService) {
    this.technicalMixedDebtPositionUpdaterService = technicalMixedDebtPositionUpdaterService;
    this.mapper = mapper;
    this.technicalDpHandlerService = technicalDpHandlerService;
  }

  public void handle(DebtPosition primaryOrgDp, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    List<DebtPosition> newMixedTechnicalDebtPositions = technicalMixedDebtPositionUpdaterService.update(primaryOrgDp, accessToken);
    for (DebtPosition newMixedTechnicalDebtPosition :  newMixedTechnicalDebtPositions) {
      technicalDpHandlerService.publishTechDp(mapper.mapToDto(newMixedTechnicalDebtPosition), receiptDTO);
    }
  }
}
