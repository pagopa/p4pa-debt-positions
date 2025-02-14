package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptMapper;
import it.gov.pagopa.pu.debtpositions.repository.ReceiptPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.receipt.ReceiptDetailPIIViewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReceiptServiceImpl implements ReceiptService{

  private final ReceiptPIIRepository receiptPIIRepository;
  private final ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository;
  private final ReceiptMapper receiptMapper;

  public ReceiptServiceImpl(ReceiptPIIRepository receiptPIIRepository,
    ReceiptDetailPIIViewRepository receiptDetailPIIViewRepository, ReceiptMapper receiptMapper) {
    this.receiptPIIRepository = receiptPIIRepository;
    this.receiptDetailPIIViewRepository = receiptDetailPIIViewRepository;
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
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId) {
    return receiptDetailPIIViewRepository.getReceiptDetail(receiptId, operatorExternalUserId);
  }

}
