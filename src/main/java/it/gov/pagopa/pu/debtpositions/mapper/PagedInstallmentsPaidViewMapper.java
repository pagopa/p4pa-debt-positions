package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PagedInstallmentsPaidViewMapper {

  private final InstallmentPaidPIIMapper installmentPaidPIIMapper;

  public PagedInstallmentsPaidViewMapper(InstallmentPaidPIIMapper installmentPaidPIIMapper) {
    this.installmentPaidPIIMapper = installmentPaidPIIMapper;
  }

  public PagedInstallmentsPaidView mapToPagedInstallmentsPaidView(Float versionTrack, Page<InstallmentPaidViewNoPII> pagedInstallmentPaidViewNoPIIDTO){
    PagedInstallmentsPaidView mappedPagedInstallmentsPaidView = new PagedInstallmentsPaidView();
    if(pagedInstallmentPaidViewNoPIIDTO != null){
      if (!pagedInstallmentPaidViewNoPIIDTO.getContent().isEmpty()){
        mappedPagedInstallmentsPaidView.setContent(pagedInstallmentPaidViewNoPIIDTO.stream().map(p -> installmentPaidPIIMapper.mapToInstallmentPaidViewDTO(versionTrack, p)).toList());
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

