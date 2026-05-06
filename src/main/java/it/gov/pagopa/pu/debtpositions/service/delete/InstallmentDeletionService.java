package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

import java.util.Set;

public interface InstallmentDeletionService {

  void deleteDraftInstallments(DebtPositionDTO debtPositionDTO, Set<Long> installmentIds, String accessToken);
}
