package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface CreateDebtPositionService {

  DebtPositionDTO createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, Boolean generateIuv);
}
