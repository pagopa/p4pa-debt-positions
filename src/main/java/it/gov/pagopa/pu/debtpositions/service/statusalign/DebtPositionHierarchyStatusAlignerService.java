package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

import java.util.Map;

public interface DebtPositionHierarchyStatusAlignerService {

  DebtPositionDTO finalizeSyncStatus(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO);

  DebtPositionDTO notifyReportedTransferId(Long transferId);

  DebtPositionDTO checkAndUpdateInstallmentExpiration(Long debtPositionId);

  DebtPositionDTO alignHierarchyStatus(DebtPosition debtPosition);
}
