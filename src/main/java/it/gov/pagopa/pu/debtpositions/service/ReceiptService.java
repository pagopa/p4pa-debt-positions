package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;

public interface ReceiptService {
  ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO);
  ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId);
}
