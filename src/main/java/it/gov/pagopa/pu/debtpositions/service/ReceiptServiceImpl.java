package it.gov.pagopa.pu.debtpositions.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedReceiptsArchivingView;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptArchivingPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

import static it.gov.pagopa.pu.debtpositions.util.SecurityUtils.SYSTEM_USERID_PREFIX;

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
    return receiptPIIRepository.findById(receiptId);
  }

  @Override
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId, Long organizationId, String iud) {
    if(StringUtils.isNotBlank(operatorExternalUserId) && !operatorExternalUserId.startsWith(SYSTEM_USERID_PREFIX)){
      return receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud);
    }else{
      return receiptDetailPIIViewRepository.getReceiptDetail(receiptId, organizationId, iud);
    }
  }

  @Override
  public PagedReceiptsArchivingView getPagedReceiptArchivingView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Pageable pageable) {
    return receiptArchivingPIIViewRepository.getPagedReceiptsArchivingView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, pageable);
  }
}
