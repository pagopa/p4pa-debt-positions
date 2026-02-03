package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.mapper.PagedInstallmentsViewMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class InstallmentPIIViewRepositoryImpl implements InstallmentPIIViewRepository {

  private final InstallmentNoPIIViewRepository installmentNoPIIViewRepository;
  private final PagedInstallmentsViewMapper pagedInstallmentsViewMapper;

  public InstallmentPIIViewRepositoryImpl(InstallmentNoPIIViewRepository installmentNoPIIViewRepository,
                                          PagedInstallmentsViewMapper pagedInstallmentsViewMapper) {
    this.installmentNoPIIViewRepository = installmentNoPIIViewRepository;
    this.pagedInstallmentsViewMapper = pagedInstallmentsViewMapper;
  }

  @Override
  public PagedInstallmentsView getPagedInstallmentsByFilters(InstallmentsSearchFiltersDTO installmentsSearchFiltersDTO,
                                                             Pageable pageable) {
    return pagedInstallmentsViewMapper.mapToPagedInstallmentsView(
      installmentNoPIIViewRepository.findInstallmentsByFilters(installmentsSearchFiltersDTO, pageable)
    );
  }
}
