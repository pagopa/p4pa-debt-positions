package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface WorkflowService {

  WorkflowCreatedDTO syncDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, PaymentEventType paymentEventType, String accessToken);
}
