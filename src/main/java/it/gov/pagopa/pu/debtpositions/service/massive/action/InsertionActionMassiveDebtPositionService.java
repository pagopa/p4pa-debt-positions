package it.gov.pagopa.pu.debtpositions.service.massive.action;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

public interface InsertionActionMassiveDebtPositionService {

  String handleInsertion(DebtPositionDTO debtPositionSynchronizeDTO, DebtPosition debtPosition, String accessToken, String operatorExternalUserId);
}
