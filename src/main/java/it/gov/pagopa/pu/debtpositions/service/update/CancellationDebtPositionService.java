package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

import java.util.List;

public interface CancellationDebtPositionService {

  String cancelInstallment(DebtPositionDTO debtPositionDTO, List<Long> installmentIds, Boolean massive, String accessToken);
}
