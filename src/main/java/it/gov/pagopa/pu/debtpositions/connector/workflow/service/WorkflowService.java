package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface WorkflowService {

  WorkflowCreatedDTO handleDpSync(DebtPositionDTO debtPositionDTO, String accessToken);

  WorkflowCreatedDTO alignDpSyncAca(DebtPositionDTO debtPositionDTO, String accessToken);

  WorkflowCreatedDTO alignDpSyncGpdPreload(DebtPositionDTO debtPositionDTO, String accessToken);

  WorkflowCreatedDTO alignDpSyncAcaGpdPreload(DebtPositionDTO debtPositionDTO, String accessToken);

  WorkflowCreatedDTO alignDpGPD(DebtPositionDTO debtPositionDTO, String accessToken);
}
