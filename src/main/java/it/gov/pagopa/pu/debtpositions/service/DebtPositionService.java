package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface DebtPositionService {

  DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO, String orgFiscalCode);
}
