package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowApiClient;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl implements WorkflowService {
  private final WorkflowApiClient workflowApiClient;

  public WorkflowServiceImpl(WorkflowApiClient workflowApiClient) {
    this.workflowApiClient = workflowApiClient;
  }

  @Override
  public WorkflowCreatedDTO handleDpSync(DebtPositionDTO debtPositionDTO, String accessToken) {
    return this.workflowApiClient.handleDpSync(debtPositionDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncAca(DebtPositionDTO debtPositionDTO, String accessToken) {
    return this.workflowApiClient.alignDpSyncAca(debtPositionDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncGpdPreload(DebtPositionDTO debtPositionDTO, String accessToken) {
    return this.workflowApiClient.alignDpSyncGpdPreload(debtPositionDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpSyncAcaGpdPreload(DebtPositionDTO debtPositionDTO, String accessToken) {
    return this.workflowApiClient.alignDpSyncAcaGpdPreload(debtPositionDTO, accessToken);
  }

  @Override
  public WorkflowCreatedDTO alignDpGPD(DebtPositionDTO debtPositionDTO, String accessToken) {
    return this.workflowApiClient.alignDpGPD(debtPositionDTO, accessToken);
  }
}
