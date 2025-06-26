package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DebtPositionService {

  void saveDebtPosition(DebtPositionDTO debtPositionDTO);
  void saveDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO mapDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO getDebtPosition(Long debtPositionId);
  DebtPosition getDebtPositionNoPII(Long debtPositionId);

  DebtPositionDTO getDebtPositionByInstallmentId(Long installmentId);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin);

  PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable);
  void delete(DebtPosition debtPosition);
}
