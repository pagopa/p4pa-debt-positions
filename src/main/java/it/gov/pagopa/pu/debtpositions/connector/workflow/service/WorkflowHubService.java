package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

public interface WorkflowHubService {

  String waitWorkflowCompletion(String accessToken, String workflowId, Integer maxAttempts, Integer retryDelayMs);
}
