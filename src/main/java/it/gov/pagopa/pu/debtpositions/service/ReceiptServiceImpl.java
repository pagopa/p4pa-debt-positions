package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {

  private final ReceiptPIIRepository receiptPIIRepository;

  public ReceiptServiceImpl(ReceiptPIIRepository receiptPIIRepository) {
    this.receiptPIIRepository = receiptPIIRepository;
  }

  @Override
  public ReceiptDTO getReceiptDetail(Long receiptId) {
    return receiptPIIRepository.getReceiptDetail(receiptId);
  }

}
