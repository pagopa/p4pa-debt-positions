package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;

public interface ReceiptPIIRepository {

  long save(Receipt receipt);
}
