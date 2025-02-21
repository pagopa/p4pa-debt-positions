package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface CancellationDebtPositionService {

  String cancelInstallment(DebtPositionDTO debtPositionDTO, Long installmentId, Boolean massive, String accessToken);
}
