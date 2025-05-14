package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SyncStatusUpdateRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferReportedRequest;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface DebtPositionHierarchyStatusAlignerService {

  DebtPositionDTO finalizeSyncStatus(Long debtPositionId, SyncStatusUpdateRequestDTO syncStatusDTO);

  /**
   * It will set the installment as REPORTED and then invoking sync debtPosition in order to publish the event
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, WorkflowCreatedDTO> notifyReportedTransferId(Long transferId, TransferReportedRequest transferReportedRequest, String accessToken);

  /**
   * It will verify installment expiration and then invoking sync debtPosition in order to publish the event and re-schedule expiration check
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, WorkflowCreatedDTO> checkAndUpdateInstallmentExpiration(Long debtPositionId, String accessToken);

  void alignHierarchyStatus(BaseDebtPosition debtPosition);

  /**
   * It will verify if debt position is draft and then invoking sync debtPosition in order to publish the debt position
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, WorkflowCreatedDTO> publishDebtPosition(Long debtPositionId, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);
}
