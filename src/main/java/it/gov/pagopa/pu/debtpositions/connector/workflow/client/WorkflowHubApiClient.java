package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.exception.common.RestInvokeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkflowHubApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowHubApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public String waitWorkflowCompletion(String accessToken, String workflowId, Integer maxAttempts, Integer retryDelayMs){
    try {
      return workflowApisHolder.getWorkflowApi(accessToken).waitWorkflowCompletion(workflowId, maxAttempts, retryDelayMs).getStatus();
    } catch (RestInvokeNotFoundException e) {
      log.info("Cannot find workflow with id {}", workflowId);
      return null;
    }
  }

}
