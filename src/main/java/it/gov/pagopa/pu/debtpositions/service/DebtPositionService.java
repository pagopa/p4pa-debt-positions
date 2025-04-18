package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.data.domain.Pageable;

public interface DebtPositionService {

  void saveDebtPosition(DebtPositionDTO debtPositionDTO);
  void saveDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO mapDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO getDebtPosition(Long debtPositionId);

  PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable);
}
