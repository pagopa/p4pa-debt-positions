package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReceiptServiceImpl implements ReceiptService {

  private final ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository;

  public ReceiptServiceImpl(ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository) {
    this.receiptDetailPIIViewRepository = receiptDetailPIIViewRepository;
  }

  @Override
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId) {
    return receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId);
  }

}
