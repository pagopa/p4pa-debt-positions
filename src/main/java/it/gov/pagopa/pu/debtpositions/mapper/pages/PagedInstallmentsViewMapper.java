package it.gov.pagopa.pu.debtpositions.mapper.pages;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.InstallmentViewDTOMapper;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PagedInstallmentsViewMapper {
  private final InstallmentViewDTOMapper mapper;

  public PagedInstallmentsViewMapper(InstallmentViewDTOMapper mapper) {
    this.mapper = mapper;
  }

  public PagedInstallmentsView mapToPagedInstallmentsView(Page<InstallmentViewNoPII> pagedInstallmentViewNoPII) {
    PagedInstallmentsView mappedPagedInstallmentsView = new PagedInstallmentsView();
    if (pagedInstallmentViewNoPII != null) {
      if (!pagedInstallmentViewNoPII.getContent().isEmpty()) {
        mappedPagedInstallmentsView.setContent(
          mapper.mapAll(pagedInstallmentViewNoPII.getContent())
        );
      } else {
        mappedPagedInstallmentsView.setContent(Collections.emptyList());
      }
      if (pagedInstallmentViewNoPII.getPageable().isPaged()) {
        mappedPagedInstallmentsView.setTotalPages((long) pagedInstallmentViewNoPII.getTotalPages());
        mappedPagedInstallmentsView.setSize((long) pagedInstallmentViewNoPII.getSize());
        mappedPagedInstallmentsView.setNumber((long) pagedInstallmentViewNoPII.getNumber());
        mappedPagedInstallmentsView.setTotalElements(pagedInstallmentViewNoPII.getTotalElements());
      }
    }
    return mappedPagedInstallmentsView;
  }
}
