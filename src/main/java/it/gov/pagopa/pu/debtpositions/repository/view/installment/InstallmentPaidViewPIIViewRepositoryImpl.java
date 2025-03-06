package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.exception.custom.TooManyElementsException;
import it.gov.pagopa.pu.debtpositions.mapper.PagedInstallmentsPaidViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class InstallmentPaidViewPIIViewRepositoryImpl implements InstallmentPaidViewPIIViewRepository{

  private final int maxTotalElements;

  private final InstallmentPaidViewNoPIIDTORepository installmentPaidViewNoPIIDTORepository;

  private final PagedInstallmentsPaidViewMapper pagedInstallmentsPaidViewMapper;

  public InstallmentPaidViewPIIViewRepositoryImpl(@Value("${installment-paid-view.max-total-elements}")int maxTotalElements, InstallmentPaidViewNoPIIDTORepository installmentPaidViewNoPIIDTORepository, PagedInstallmentsPaidViewMapper pagedInstallmentsPaidViewMapper) {
    this.maxTotalElements = maxTotalElements;
    this.installmentPaidViewNoPIIDTORepository = installmentPaidViewNoPIIDTORepository;
    this.pagedInstallmentsPaidViewMapper = pagedInstallmentsPaidViewMapper;
  }


  @Override
  public PagedInstallmentsPaidView getPagedInstallmentPaidView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable) {

    Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPIIDTO = installmentPaidViewNoPIIDTORepository.findInstallmentPaidViewNoPIIDTO(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, pageable);

    if(pagedInstallmentPaidViewNoPIIDTO.getTotalElements() > maxTotalElements){
      throw new TooManyElementsException("The number of InstallmentPaidViewNoPII records returned: %d exceeds the maximum allowed: %d".formatted(pagedInstallmentPaidViewNoPIIDTO.getTotalElements(), maxTotalElements));
    }
    return pagedInstallmentsPaidViewMapper.mapToPagedInstallmentsPaidView(pagedInstallmentPaidViewNoPIIDTO);
  }
}
