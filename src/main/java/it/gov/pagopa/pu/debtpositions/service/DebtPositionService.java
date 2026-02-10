package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface DebtPositionService {

  void saveDebtPosition(DebtPositionDTO debtPositionDTO);
  void saveDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO mapDebtPosition(DebtPosition debtPosition);
  DebtPositionDTO getDebtPosition(Long debtPositionId);
  DebtPosition getDebtPositionNoPII(Long debtPositionId);
  Optional<DebtPosition> getDebtPositionByIupdAndOrganizationId(String iupd, Long organizationId);

  PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, List<InstallmentStatus> statusToExclude, Pageable pageable);
  DebtPositionDTO getDebtPositionByInstallmentId(Long installmentId);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigins);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin);
  List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin);
  List<DebtPositionDTO> getDebtPositionsByDebtorFiscalCodeAndDebtorEntityType(String debtorFiscalCode, PersonEntityType debtorEntityType, List<InstallmentStatus> status, List<DebtPositionOrigin> debtPositionOrigin, List<String> debtPositionTypeOrgCodesToExclude, List<Long> organizationIds, LocalDateTimeIntervalFilter dateTimeIntervalFilter);
  PagedDebtorUnpaidDebtPositionDTO getPagedDebtorUnpaidDebtPosition(String xFiscalCode, List<Long> organizationIds, Pageable pageable);
  DebtorDebtPositionDTO getDebtorUnpaidDebtPositionOverview(Long debtPositionId, String xFiscalCode, Long organizationId);
  void delete(DebtPosition debtPosition);
}
