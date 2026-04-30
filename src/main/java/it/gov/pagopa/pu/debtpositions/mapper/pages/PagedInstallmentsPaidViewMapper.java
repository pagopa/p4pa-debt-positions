package it.gov.pagopa.pu.debtpositions.mapper.pages;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.InstallmentPaidPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PagedInstallmentsPaidViewMapper {

  private final InstallmentPaidPIIMapper mapper;

  public PagedInstallmentsPaidViewMapper(InstallmentPaidPIIMapper mapper) {
    this.mapper = mapper;
  }

  public PagedInstallmentsPaidView mapToPagedInstallmentsPaidView(Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPIIDTO){
    PagedInstallmentsPaidView mappedPagedInstallmentsPaidView = new PagedInstallmentsPaidView();
    if(pagedInstallmentPaidViewNoPIIDTO != null){
      if (!pagedInstallmentPaidViewNoPIIDTO.getContent().isEmpty()){
        mappedPagedInstallmentsPaidView.setContent(mapper.mapAll(pagedInstallmentPaidViewNoPIIDTO.getContent()));
      }else {
        mappedPagedInstallmentsPaidView.setContent(Collections.emptyList());
      }

      if(pagedInstallmentPaidViewNoPIIDTO.getPageable().isPaged()){
        mappedPagedInstallmentsPaidView.setTotalPages((long) pagedInstallmentPaidViewNoPIIDTO.getTotalPages());
        mappedPagedInstallmentsPaidView.setSize((long) pagedInstallmentPaidViewNoPIIDTO.getSize());
        mappedPagedInstallmentsPaidView.setNumber((long) pagedInstallmentPaidViewNoPIIDTO.getNumber());
        mappedPagedInstallmentsPaidView.setTotalElements(pagedInstallmentPaidViewNoPIIDTO.getTotalElements());
      }
    }
    return mappedPagedInstallmentsPaidView;
  }
}

