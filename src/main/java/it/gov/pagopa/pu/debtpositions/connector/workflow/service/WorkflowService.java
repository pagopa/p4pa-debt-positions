package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface WorkflowService {

  WorkflowCreatedDTO handleDpSync(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken);

  WorkflowCreatedDTO alignDpSyncAca(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken);

  WorkflowCreatedDTO alignDpSyncGpdPreload(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken);

  WorkflowCreatedDTO alignDpSyncAcaGpdPreload(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken);

  WorkflowCreatedDTO alignDpGPD(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken);
}
