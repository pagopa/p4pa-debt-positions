package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowApiClient;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowServiceImpl implements WorkflowService {
  private final WorkflowApiClient workflowApiClient;

  public WorkflowServiceImpl(WorkflowApiClient workflowApiClient) {
    this.workflowApiClient = workflowApiClient;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, PaymentEventType paymentEventType, String accessToken) {
    return this.workflowApiClient.syncDebtPosition(debtPositionDTO, massive, paymentEventType, accessToken);
  }

}
