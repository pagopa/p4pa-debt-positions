package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowDebtPositionApiClient;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowDebtPositionServiceImpl implements WorkflowDebtPositionService {
  private final WorkflowDebtPositionApiClient workflowApiClient;

  public WorkflowDebtPositionServiceImpl(WorkflowDebtPositionApiClient workflowApiClient) {
    this.workflowApiClient = workflowApiClient;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, PaymentEventType paymentEventType, String eventDescription, String accessToken) {
    return this.workflowApiClient.syncDebtPosition(debtPositionDTO, wfExecutionParameters, paymentEventType, eventDescription, accessToken);
  }

}
