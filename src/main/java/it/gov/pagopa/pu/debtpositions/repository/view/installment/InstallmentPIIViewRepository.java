package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import org.springframework.data.domain.Pageable;

public interface InstallmentPIIViewRepository {
  PagedInstallmentsView getPagedInstallmentsByFilters(InstallmentsSearchFiltersDTO installmentsSearchFiltersDTO, Pageable pageable);
}
