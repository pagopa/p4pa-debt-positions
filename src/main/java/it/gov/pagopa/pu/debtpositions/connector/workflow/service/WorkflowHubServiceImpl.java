package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowHubApiClient;
import org.springframework.stereotype.Service;

@Service
public class WorkflowHubServiceImpl implements WorkflowHubService {

  private final WorkflowHubApiClient workflowHubApiClient;

  public WorkflowHubServiceImpl(WorkflowHubApiClient workflowHubApiClient) {
    this.workflowHubApiClient = workflowHubApiClient;
  }

  @Override
  public String waitWorkflowCompletion(String accessToken, String workflowId, Integer maxAttempts, Integer retryDelayMs) {
    return workflowHubApiClient.waitWorkflowCompletion(accessToken, workflowId, maxAttempts, retryDelayMs);

  }
}
