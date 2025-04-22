package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ManageDebtPositionDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface DebtPositionManageInstallmentsService {

  /** It will manage DebtPosition installments and return the updated DebtPosition and the workflowId launched */
  Pair<DebtPositionDTO, String> manageDebtPositionInstallments(Long debtPositionId, ManageDebtPositionDTO manageDebtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);
}
