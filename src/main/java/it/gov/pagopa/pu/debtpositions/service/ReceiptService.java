package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;

public interface ReceiptService {
  ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO);
  ReceiptDTO getReceiptDetail(Long receiptId);
}
