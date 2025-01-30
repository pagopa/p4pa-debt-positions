package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface DebtPositionSyncService {

  void invokeWorkFlow(DebtPositionDTO debtPositionDTO, String accessToken);
}
