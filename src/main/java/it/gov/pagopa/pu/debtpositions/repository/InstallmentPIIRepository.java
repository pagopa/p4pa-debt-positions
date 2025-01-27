package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Installment;

public interface InstallmentPIIRepository {

  long save(Installment installment);
}
