package it.gov.pagopa.pu.debtpositions.service.create.receipt.mixed;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp.UpdateAndSynchronizeTechDp;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class MixedDpPaymentHandlerService {

  private final TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterService;
  private final DebtPositionMapper mapper;
  private final UpdateAndSynchronizeTechDp updateAndSynchronizeTechDp;

  public MixedDpPaymentHandlerService(TechnicalMixedDebtPositionUpdaterService technicalMixedDebtPositionUpdaterService, DebtPositionMapper mapper, UpdateAndSynchronizeTechDp updateAndSynchronizeTechDp) {
    this.technicalMixedDebtPositionUpdaterService = technicalMixedDebtPositionUpdaterService;
    this.mapper = mapper;
    this.updateAndSynchronizeTechDp = updateAndSynchronizeTechDp;
  }

  public void handle(DebtPosition primaryOrgDp, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    List<DebtPosition> newMixedTechnicalDebtPositions = technicalMixedDebtPositionUpdaterService.update(primaryOrgDp, receiptDTO.getPaymentDateTime(), accessToken);
    if(!CollectionUtils.isEmpty(newMixedTechnicalDebtPositions)) {
      List<DebtPositionDTO> fullDtos = mapper.mapAllToDto(newMixedTechnicalDebtPositions);
      for (DebtPositionDTO dp : fullDtos) {
        updateAndSynchronizeTechDp.publishTechDp(dp, receiptDTO, accessToken);
      }
    }
  }
}
