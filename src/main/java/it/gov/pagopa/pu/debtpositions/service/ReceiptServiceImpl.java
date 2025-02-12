package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import org.springframework.stereotype.Service;

@Service
public class ReceiptServiceImpl implements ReceiptService{

  private final ReceiptPIIRepository receiptPIIRepository;
  private final ReceiptMapper receiptMapper;

  public ReceiptServiceImpl(ReceiptPIIRepository receiptPIIRepository, ReceiptMapper receiptMapper) {
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptMapper = receiptMapper;
  }

  @Override
  public ReceiptDTO createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    Receipt receipt = receiptMapper.mapToModel(receiptDTO);
    long newId = receiptPIIRepository.save(receipt);
    receiptDTO.setReceiptId(newId);
    return receiptDTO;
  }

  @Override
  public ReceiptDTO getReceiptDetail(Long receiptId) {
    return receiptPIIRepository.getReceiptDetail(receiptId);
  }

}
