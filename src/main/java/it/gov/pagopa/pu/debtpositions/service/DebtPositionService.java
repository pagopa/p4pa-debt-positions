package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;

import java.util.Map;

public interface DebtPositionService {

  void saveDebtPosition(DebtPositionDTO debtPositionDTO);
  DebtPositionDTO finalizeSyncStatus(Long debtPositionId, Map<String, IudSyncStatusUpdateDTO> syncStatusDTO);
}
