package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowApiClient;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl implements WorkflowService {
  private final WorkflowApiClient workflowApiClient;

  public WorkflowServiceImpl(WorkflowApiClient workflowApiClient) {
    this.workflowApiClient = workflowApiClient;
  }

  @Override
  public WorkflowCreatedDTO handleDpSync(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return this.workflowApiClient.handleDpSync(debtPositionRequestDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncAca(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return this.workflowApiClient.alignDpSyncAca(debtPositionRequestDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncGpdPreload(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return this.workflowApiClient.alignDpSyncGpdPreload(debtPositionRequestDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncAcaGpdPreload(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return this.workflowApiClient.alignDpSyncAcaGpdPreload(debtPositionRequestDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpGPD(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return this.workflowApiClient.alignDpGPD(debtPositionRequestDTO, accessToken);
  }
}
