package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import org.springframework.stereotype.Service;

@Service
public class WorkflowHubApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowHubApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public String waitWorkflowCompletion(String accessToken, String workflowId, Integer maxAttempts, Integer retryDelayMs){
    return workflowApisHolder.getWorkflowApi(accessToken).waitWorkflowCompletion(workflowId, maxAttempts, retryDelayMs);
  }

}
