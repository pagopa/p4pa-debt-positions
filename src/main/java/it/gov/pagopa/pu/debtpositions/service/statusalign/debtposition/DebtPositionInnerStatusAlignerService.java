package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;

public interface DebtPositionInnerStatusAlignerService {

  void updateDebtPositionStatus(BaseDebtPosition debtPosition);
}
