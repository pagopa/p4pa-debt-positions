package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptArchivingPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ReceiptServiceImpl implements ReceiptService {

  private final ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository;
  private final ReceiptArchivingPIIViewRepository receiptArchivingPIIViewRepository;

  public ReceiptServiceImpl(ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository, ReceiptArchivingPIIViewRepository receiptArchivingPIIViewRepository) {
    this.receiptDetailPIIViewRepository = receiptDetailPIIViewRepository;
    this.receiptArchivingPIIViewRepository = receiptArchivingPIIViewRepository;
  }

  @Override
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId) {
    return receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId);
  }

  @Override
  public PagedReceiptsArchivingView getPagedReceiptArchivingView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable) {
    return receiptArchivingPIIViewRepository.getPagedReceiptsArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, pageable);
  }
}
