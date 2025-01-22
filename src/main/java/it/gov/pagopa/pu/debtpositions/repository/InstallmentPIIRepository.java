package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

public interface InstallmentPIIRepository {

  long save(Installment installment);

  long countExistingDebtPosition(DebtPosition debtPosition);
}
