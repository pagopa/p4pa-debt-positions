package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.filters.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.mapper.PagedInstallmentsViewMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class InstallmentViewPIIRepositoryImpl implements InstallmentViewPIIRepository {

  private final InstallmentViewNoPIIRepository installmentViewNoPIIRepository;
  private final PagedInstallmentsViewMapper pagedInstallmentsViewMapper;

  public InstallmentViewPIIRepositoryImpl(InstallmentViewNoPIIRepository installmentViewNoPIIRepository,
                                          PagedInstallmentsViewMapper pagedInstallmentsViewMapper) {
    this.installmentViewNoPIIRepository = installmentViewNoPIIRepository;
    this.pagedInstallmentsViewMapper = pagedInstallmentsViewMapper;
  }

  @Override
  public PagedInstallmentsView getPagedInstallmentsByFilters(InstallmentsSearchFiltersDTO installmentsSearchFiltersDTO,
                                                             Pageable pageable) {
    return pagedInstallmentsViewMapper.mapToPagedInstallmentsView(
      installmentViewNoPIIRepository.findInstallmentsByFilters(installmentsSearchFiltersDTO, pageable)
    );
  }
}
