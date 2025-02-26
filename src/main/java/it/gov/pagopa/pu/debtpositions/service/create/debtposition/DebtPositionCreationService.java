package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface DebtPositionCreationService {

  DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId);
}
