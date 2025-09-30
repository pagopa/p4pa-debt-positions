package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DebtPositionService {

  void saveDebtPosition(DebtPositionDTO debtPositionDTO);
  void saveDebtPosition(DebtPosition debtPosition);
  void updateDebtPosition(Long debtPositionId, DebtPositionDTO debtPositionDTO);
  DebtPositionDTO mapDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO getDebtPosition(Long debtPositionId);
  DebtPosition getDebtPositionNoPII(Long debtPositionId);
  Optional<DebtPosition> getDebtPositionByIupdAndOrganizationId(String iupd, Long organizationId);

  PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, List<InstallmentStatus> statusToExclude, Pageable pageable);
  DebtPositionDTO getDebtPositionByInstallmentId(Long installmentId);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin);

  void delete(DebtPosition debtPosition);
}
