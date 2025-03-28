package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface DebtPositionHierarchyStatusAlignerService {

  DebtPositionDTO finalizeSyncStatus(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO);

  /**
   * It will set the installment as REPORTED and then invoking sync debtPosition in order to publish the event
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, String> notifyReportedTransferId(Long transferId, String accessToken);

  /**
   * It will verify installment expiration and then invoking sync debtPosition in order to publish the event and re-schedule expiration check
   * @return The DebtPosition updated and workflowId started
   */
  Pair<DebtPositionDTO, String> checkAndUpdateInstallmentExpiration(Long debtPositionId, String accessToken);

  void alignHierarchyStatus(DebtPosition debtPosition);
  /** Call only if PII should be resolved */
  DebtPositionDTO alignHierarchyStatusAndRemap(DebtPosition debtPosition);
}
