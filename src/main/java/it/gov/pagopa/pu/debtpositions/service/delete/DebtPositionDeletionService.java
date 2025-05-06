package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionDeletionService {

  WorkflowCreatedDTO deleteDebtPosition(Long debtPositionId, String accessToken, String operatorExternalUserId);
}
