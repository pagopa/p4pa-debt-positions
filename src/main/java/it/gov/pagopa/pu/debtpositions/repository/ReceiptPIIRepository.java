package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;

public interface ReceiptPIIRepository {

  long save(Receipt receipt);
  ReceiptDTO getReceiptDetail(Long receiptId, String orgFiscalCode);
}
