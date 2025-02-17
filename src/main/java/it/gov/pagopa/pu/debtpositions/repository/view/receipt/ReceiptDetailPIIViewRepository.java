package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;

public interface ReceiptDetailPIIViewRepository {
  ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId);
}
