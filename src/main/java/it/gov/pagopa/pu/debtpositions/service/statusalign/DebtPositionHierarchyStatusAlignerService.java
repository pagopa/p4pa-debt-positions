package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferReportedRequest;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface DebtPositionHierarchyStatusAlignerService {

  DebtPositionDTO finalizeSyncStatus(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO);

  /**
   * It will set the installment as REPORTED and then invoking sync debtPosition in order to publish the event
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, String> notifyReportedTransferId(Long transferId, TransferReportedRequest transferReportedRequest, String accessToken);

  /**
   * It will verify installment expiration and then invoking sync debtPosition in order to publish the event and re-schedule expiration check
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, String> checkAndUpdateInstallmentExpiration(Long debtPositionId, String accessToken);

  void alignHierarchyStatus(BaseDebtPosition debtPosition);
}
