package it.gov.pagopa.pu.debtpositions.service.massive.action;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface UpdateActionMassiveDebtPositionService {

  String handleModification(DebtPositionDTO debtPositionSynchronizeDTO, String accessToken);
  String handleCancellation(DebtPositionDTO debtPositionSynchronizeDTO, String accessToken);
}
