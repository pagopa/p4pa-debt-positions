package it.gov.pagopa.pu.debtpositions.service.sync;

import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowDebtPositionService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionSyncServiceImpl implements DebtPositionSyncService {

  private final WorkflowDebtPositionService workflowService;

  public DebtPositionSyncServiceImpl(WorkflowDebtPositionService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, PaymentEventType paymentEventType, String eventDescription, String accessToken) {
    return workflowService.syncDebtPosition(debtPositionDTO, wfExecutionParameters, paymentEventType, eventDescription, accessToken);
  }

}
