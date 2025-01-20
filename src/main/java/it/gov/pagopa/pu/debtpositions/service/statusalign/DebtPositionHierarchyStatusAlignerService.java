package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;

import java.util.Map;

public interface DebtPositionHierarchyStatusAlignerService {

  void finalizeSyncStatus(Long debtPositionId, Map<String, IudSyncStatusUpdateDTO> syncStatusDTO);
}
