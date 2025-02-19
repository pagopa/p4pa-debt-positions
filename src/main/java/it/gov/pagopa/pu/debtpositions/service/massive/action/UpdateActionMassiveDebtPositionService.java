package it.gov.pagopa.pu.debtpositions.service.massive.action;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface UpdateActionMassiveDebtPositionService {

  String handleUpdate(DebtPositionDTO debtPositionSynchronizeDTO, Boolean massive, String accessToken);
  String handleCancellation(DebtPositionDTO debtPositionSynchronizeDTO, Boolean massive, String accessToken);
}
