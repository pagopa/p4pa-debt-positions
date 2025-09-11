package it.gov.pagopa.pu.debtpositions.service.update;


import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

import java.util.List;

public interface TechnicalMixedDebtPositionUpdaterService {
  List<DebtPosition> update(DebtPosition debtPosition);
}
