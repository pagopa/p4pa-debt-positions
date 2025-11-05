package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;

public interface ReceiptFileService {
  FileResourceDTO generateReceiptPdf(Long receiptId, Long organizationId);
}
