package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface ReceiptService {
  ReceiptDTO getReceipt(Long receiptId);
  ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId, Long organizationId, String iud);
  PagedReceiptsArchivingView getPagedReceiptArchivingView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Pageable pageable);
}
