package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;

public interface ReceiptPIIRepository {

  ReceiptDTO save(ReceiptDTO receipt);
  ReceiptDTO findById(Long receiptId);
}
