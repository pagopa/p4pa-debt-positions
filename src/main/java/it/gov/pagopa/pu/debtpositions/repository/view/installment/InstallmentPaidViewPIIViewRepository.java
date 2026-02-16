package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.filters.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import org.springframework.data.domain.Pageable;

public interface InstallmentPaidViewPIIViewRepository {
  PagedInstallmentsPaidView getPagedInstallmentPaidView(ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO, Pageable pageable);
}
