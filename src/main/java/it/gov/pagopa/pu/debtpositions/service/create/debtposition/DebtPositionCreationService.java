package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;

public interface DebtPositionCreationService {

  DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, DebtPositionOrigin debtPositionOrigin, Boolean massive, String accessToken, String operatorExternalUserId);
}
