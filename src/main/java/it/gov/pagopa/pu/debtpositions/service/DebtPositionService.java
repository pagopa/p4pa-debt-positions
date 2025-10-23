package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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
  List<DebtPositionDTO> getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(String debtorFiscalCode, PersonEntityType debtorEntityType, List<InstallmentStatus> status, List<DebtPositionOrigin> debtPositionOrigin, Long organizationId, LocalDateTime dateFrom, LocalDateTime dateTo);

  void delete(DebtPosition debtPosition);
}
