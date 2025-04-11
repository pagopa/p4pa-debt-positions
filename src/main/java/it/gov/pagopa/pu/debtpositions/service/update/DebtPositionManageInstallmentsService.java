package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ManageDebtPositionDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface DebtPositionManageInstallmentsService {

  Pair<DebtPositionDTO, String> manageDebtPositionInstallments(Long debtPositionId, ManageDebtPositionDTO manageDebtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);
}
