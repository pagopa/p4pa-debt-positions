package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionSyncService {

  WorkflowCreatedDTO invokeWorkFlow(DebtPositionDTO debtPositionDTO, String accessToken);
}
