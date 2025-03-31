package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

public interface DebtPositionInnerStatusAlignerService {

  void updateDebtPositionStatus(DebtPosition debtPosition);
}
