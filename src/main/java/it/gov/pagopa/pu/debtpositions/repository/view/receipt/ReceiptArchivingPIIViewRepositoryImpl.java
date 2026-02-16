package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.exception.custom.ExportTooManyRecordsException;
import it.gov.pagopa.pu.debtpositions.mapper.pages.PagedReceiptsArchivingViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ReceiptArchivingPIIViewRepositoryImpl implements ReceiptArchivingPIIViewRepository {

  private final int maxTotalElements;
  private final ReceiptArchivingNoPIIViewRepository receiptArchivingNoPIIViewRepository;
  private final PagedReceiptsArchivingViewMapper pagedReceiptsArchivingViewMapper;

  public ReceiptArchivingPIIViewRepositoryImpl(@Value("${data-export.receipt-archiving-view.max-total-elements}")int maxTotalElements, ReceiptArchivingNoPIIViewRepository receiptArchivingNoPIIViewRepository, PagedReceiptsArchivingViewMapper pagedReceiptsArchivingViewMapper) {
    this.maxTotalElements = maxTotalElements;
    this.receiptArchivingNoPIIViewRepository = receiptArchivingNoPIIViewRepository;
    this.pagedReceiptsArchivingViewMapper = pagedReceiptsArchivingViewMapper;
  }

  @Override
  public PagedReceiptsArchivingView getPagedReceiptsArchivingView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Pageable pageable) {

    Page<ReceiptArchivingNoPIIView> receiptArchivingViewNoPIIDTO = receiptArchivingNoPIIViewRepository.findReceiptArchivingViewNoPIIDTO(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, pageable);

    if (receiptArchivingViewNoPIIDTO.getTotalElements() > maxTotalElements){
      throw new ExportTooManyRecordsException("[TOO_MANY_EXPORTED_RECORDS] The number of ReceiptArchivingViewNoPII records returned: %d exceeds the maximum allowed: %d".formatted(receiptArchivingViewNoPIIDTO.getTotalElements(), maxTotalElements));
    }

    return pagedReceiptsArchivingViewMapper.mapToPagedReceiptsArchivingView(receiptArchivingViewNoPIIDTO);
  }
}
