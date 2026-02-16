package it.gov.pagopa.pu.debtpositions.mapper.pages;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.ReceiptArchivingPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class PagedReceiptsArchivingViewMapper {

    private final ReceiptArchivingPIIMapper receiptArchivingPIIMapper;

  public PagedReceiptsArchivingViewMapper(ReceiptArchivingPIIMapper receiptArchivingPIIMapper) {
    this.receiptArchivingPIIMapper = receiptArchivingPIIMapper;
  }


  public PagedReceiptsArchivingView mapToPagedReceiptsArchivingView(Page<ReceiptArchivingNoPIIView> pagedReceiptArchivingViewNoPIIDTO){
      PagedReceiptsArchivingView mappedPagedReceiptsArchivingView = new PagedReceiptsArchivingView();
      if(pagedReceiptArchivingViewNoPIIDTO != null){
        if (!pagedReceiptArchivingViewNoPIIDTO.getContent().isEmpty()){
          mappedPagedReceiptsArchivingView.setContent(pagedReceiptArchivingViewNoPIIDTO.stream().map(receiptArchivingPIIMapper::map).toList());
        }else {
          mappedPagedReceiptsArchivingView.setContent(Collections.emptyList());
        }

        if(pagedReceiptArchivingViewNoPIIDTO.getPageable().isPaged()){
          mappedPagedReceiptsArchivingView.setTotalPages((long) pagedReceiptArchivingViewNoPIIDTO.getTotalPages());
          mappedPagedReceiptsArchivingView.setSize((long) pagedReceiptArchivingViewNoPIIDTO.getSize());
          mappedPagedReceiptsArchivingView.setNumber((long) pagedReceiptArchivingViewNoPIIDTO.getNumber());
          mappedPagedReceiptsArchivingView.setTotalElements(pagedReceiptArchivingViewNoPIIDTO.getTotalElements());
        }
      }
      return mappedPagedReceiptsArchivingView;
    }

}
