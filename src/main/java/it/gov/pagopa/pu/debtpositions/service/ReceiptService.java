package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;

public interface ReceiptService {
  ReceiptDTO getReceiptDetail(Long receiptId);
}
