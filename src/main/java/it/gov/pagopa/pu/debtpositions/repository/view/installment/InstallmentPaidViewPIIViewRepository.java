package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface InstallmentPaidViewPIIViewRepository {
  PagedInstallmentsPaidView getPagedInstallmentPaidView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable);
}
