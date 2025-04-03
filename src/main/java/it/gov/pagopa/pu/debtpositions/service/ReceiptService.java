package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface ReceiptService {
  ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId);
  PagedReceiptsArchivingView getPagedReceiptArchivingView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable);
}
