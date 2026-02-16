package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import static it.gov.pagopa.pu.debtpositions.util.Constants.WS_USER_PREFIX;

import it.gov.pagopa.pu.debtpositions.dto.filters.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.exception.custom.ExportTooManyRecordsException;
import it.gov.pagopa.pu.debtpositions.mapper.pages.PagedInstallmentsPaidViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InstallmentPaidViewPIIViewRepositoryImpl implements InstallmentPaidViewPIIViewRepository{

  private final int maxTotalElements;

  private final InstallmentPaidViewNoPIIDTORepository installmentPaidViewNoPIIDTORepository;

  private final PagedInstallmentsPaidViewMapper pagedInstallmentsPaidViewMapper;

  public InstallmentPaidViewPIIViewRepositoryImpl(@Value("${data-export.installment-paid-view.max-total-elements}")int maxTotalElements, InstallmentPaidViewNoPIIDTORepository installmentPaidViewNoPIIDTORepository, PagedInstallmentsPaidViewMapper pagedInstallmentsPaidViewMapper) {
    this.maxTotalElements = maxTotalElements;
    this.installmentPaidViewNoPIIDTORepository = installmentPaidViewNoPIIDTORepository;
    this.pagedInstallmentsPaidViewMapper = pagedInstallmentsPaidViewMapper;
  }

  @Override
  public PagedInstallmentsPaidView getPagedInstallmentPaidView(
    ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO,
    Pageable pageable) {
    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPIIDTO =
      exportPaidInstallmentsFiltersDTO.getOperatorExternalUserId()
        .startsWith(WS_USER_PREFIX) ?
        installmentPaidViewNoPIIDTORepository.findInstallmentPaidViewNoPIIDTOWithoutOperator(
          exportPaidInstallmentsFiltersDTO, pageable)
        : installmentPaidViewNoPIIDTORepository.findInstallmentPaidViewNoPIIDTO(
          exportPaidInstallmentsFiltersDTO, pageable);

    if (pagedInstallmentPaidViewNoPIIDTO.getTotalElements() > maxTotalElements) {
      throw new ExportTooManyRecordsException(
        "[TOO_MANY_EXPORTED_RECORDS] The number of InstallmentPaidViewNoPII records returned: %d exceeds the maximum allowed: %d".formatted(
          pagedInstallmentPaidViewNoPIIDTO.getTotalElements(),
          maxTotalElements));
    }

    return pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(
      pagedInstallmentPaidViewNoPIIDTO);
  }
}
