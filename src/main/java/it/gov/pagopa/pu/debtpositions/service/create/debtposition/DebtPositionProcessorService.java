package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;

public interface DebtPositionProcessorService {
  void updateAmounts(BaseDebtPosition debtPosition);
}
