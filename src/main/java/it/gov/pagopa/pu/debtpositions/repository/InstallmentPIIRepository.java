package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.springframework.stereotype.Repository;

@Repository
public interface InstallmentPIIRepository {

    long save(InstallmentDTO installment);
}
