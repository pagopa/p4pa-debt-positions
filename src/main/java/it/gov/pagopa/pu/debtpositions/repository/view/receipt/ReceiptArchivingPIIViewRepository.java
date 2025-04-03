package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface ReceiptArchivingPIIViewRepository {
  PagedReceiptsArchivingView getPagedReceiptsArchivingView (Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable);
}
