package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptArchivingPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ReceiptServiceImpl implements ReceiptService {

  private final ReceiptPIIRepository receiptPIIRepository;
  private final ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository;
  private final ReceiptArchivingPIIViewRepository receiptArchivingPIIViewRepository;

  public ReceiptServiceImpl(ReceiptPIIRepository receiptPIIRepository, ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository, ReceiptArchivingPIIViewRepository receiptArchivingPIIViewRepository) {
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptDetailPIIViewRepository = receiptDetailPIIViewRepository;
    this.receiptArchivingPIIViewRepository = receiptArchivingPIIViewRepository;
  }

  @Override
  public ReceiptDTO getReceipt(Long receiptId) {
    return receiptPIIRepository.getReceiptDetail(receiptId);
  }

  @Override
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId, Long organizationId) {
    return receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId, organizationId);
  }

  @Override
  public PagedReceiptsArchivingView getPagedReceiptArchivingView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Pageable pageable) {
    return receiptArchivingPIIViewRepository.getPagedReceiptsArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, pageable);
  }
}
