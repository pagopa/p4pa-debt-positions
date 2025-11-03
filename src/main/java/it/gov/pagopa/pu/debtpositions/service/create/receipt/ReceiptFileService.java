package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface ReceiptFileService {
  byte[] generateReceiptPdf(ReceiptDetailDTO receiptDetail, Organization organization);
}
