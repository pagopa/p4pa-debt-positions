package it.gov.pagopa.pu.debtpositions.service.sync;

import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DebtPositionSyncServiceImpl implements DebtPositionSyncService {

  private final WorkflowService workflowService;

  private static final Set<DebtPositionOrigin> DEBT_POSITION_ORIGIN_TO_SYNC = Set.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS
  );

  public DebtPositionSyncServiceImpl(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @Override
  public WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, PaymentEventType paymentEventType, String eventDescription, String accessToken) {
    if (DEBT_POSITION_ORIGIN_TO_SYNC.contains(debtPositionDTO.getDebtPositionOrigin())) {
      return workflowService.syncDebtPosition(debtPositionDTO, wfExecutionParameters, paymentEventType, eventDescription, accessToken);
    }
    return null;
  }

}
