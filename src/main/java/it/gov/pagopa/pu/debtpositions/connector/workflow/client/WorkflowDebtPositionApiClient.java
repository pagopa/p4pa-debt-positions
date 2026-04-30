package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.SyncDebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowDebtPositionApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowDebtPositionApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, PaymentEventType paymentEventType, String eventDescription, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .syncDebtPosition(new SyncDebtPositionRequestDTO(debtPositionDTO, wfExecutionParameters.getExecutionConfig()), wfExecutionParameters.isMassive(), wfExecutionParameters.isPartialChange(), paymentEventType, eventDescription);
  }

  public WorkflowCreatedDTO massiveDpIbanUpdate(
    Long orgId,
    MassiveDebtPositionIbanUpdateRequestDTO massiveDebtPositionIbanUpdateRequestDTO,
    String accessToken
  ) {
    return workflowApisHolder.getDebtPositionApi(accessToken).massiveDpIbanUpdate(orgId, massiveDebtPositionIbanUpdateRequestDTO);
  }
}
