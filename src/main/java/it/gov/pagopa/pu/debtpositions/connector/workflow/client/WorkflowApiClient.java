package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }


  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, PaymentEventType paymentEventType, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .syncDebtPosition(debtPositionDTO, massive, paymentEventType);
  }
}
