package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;


public interface PublishDebtPositionService {

  Pair<DebtPositionDTO, WorkflowCreatedDTO> publishDebtPosition(Long debtPositionId, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);
}
