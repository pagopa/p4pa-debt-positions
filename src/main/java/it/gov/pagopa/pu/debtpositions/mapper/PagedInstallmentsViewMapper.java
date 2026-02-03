package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PagedInstallmentsViewMapper {
  private final InstallmentViewDTOMapper installmentViewDTOMapper;

  public PagedInstallmentsViewMapper(InstallmentViewDTOMapper installmentViewDTOMapper) {
    this.installmentViewDTOMapper = installmentViewDTOMapper;
  }

  public PagedInstallmentsView mapToPagedInstallmentsView(Page<InstallmentViewNoPII> pagedInstallmentViewNoPII) {
    PagedInstallmentsView mappedPagedInstallmentsView = new PagedInstallmentsView();
    if (pagedInstallmentViewNoPII != null) {
      if (!pagedInstallmentViewNoPII.getContent().isEmpty()) {
        mappedPagedInstallmentsView.setContent(
          pagedInstallmentViewNoPII.stream().map(installmentViewDTOMapper::map).toList()
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
