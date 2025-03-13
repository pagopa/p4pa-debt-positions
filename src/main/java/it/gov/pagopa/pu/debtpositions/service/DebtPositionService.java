package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.data.domain.Pageable;

public interface DebtPositionService {

  DebtPosition saveDebtPosition(DebtPositionDTO debtPositionDTO, Organization org);
  /** Call only if you want the PII resolved in output */
  DebtPositionDTO saveDebtPositionAndRemap(DebtPositionDTO debtPositionDTO, Organization org);
  DebtPositionDTO getDebtPosition(Long debtPositionId);

  PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable);
}
