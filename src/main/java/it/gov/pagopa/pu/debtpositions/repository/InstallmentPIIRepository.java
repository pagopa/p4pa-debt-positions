package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallmentPIIRepository {

    long save(Installment installment);
}
