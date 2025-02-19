package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;

public interface ReceiptService {
  ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId);
}
