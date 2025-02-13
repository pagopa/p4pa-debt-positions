package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;

public interface CreateReceiptService {
  ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken);
}
