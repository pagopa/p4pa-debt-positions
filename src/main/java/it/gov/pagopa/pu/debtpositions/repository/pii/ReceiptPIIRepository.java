package it.gov.pagopa.pu.debtpositions.repository.pii;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;

public interface ReceiptPIIRepository {

  ReceiptDTO save(ReceiptDTO receipt);
  ReceiptDTO findById(Long receiptId);
}
