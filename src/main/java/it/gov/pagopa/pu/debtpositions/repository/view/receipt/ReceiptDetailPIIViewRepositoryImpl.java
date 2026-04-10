package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.pii.view.ReceiptDetailPIIViewMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.springframework.stereotype.Service;

@Service
public class ReceiptDetailPIIViewRepositoryImpl implements ReceiptDetailPIIViewRepository {
  private final ReceiptDetailNoPIIViewRepository receiptDetailNoPIIViewRepository;
  private final ReceiptDetailPIIViewMapper receiptDetailPIIViewMapper;

  public ReceiptDetailPIIViewRepositoryImpl(
    ReceiptDetailNoPIIViewRepository receiptDetailNoPIIViewRepository,
    ReceiptDetailPIIViewMapper receiptDetailPIIViewMapper) {
    this.receiptDetailNoPIIViewRepository = receiptDetailNoPIIViewRepository;
    this.receiptDetailPIIViewMapper = receiptDetailPIIViewMapper;
  }

  @Override
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, String operatorExternalUserId, Long organizationId, String iud) {
    ReceiptDetailNoPIIView receiptDetailNoPIIView = receiptDetailNoPIIViewRepository.findReceiptDetailView(receiptId, operatorExternalUserId, organizationId, iud)
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_RECEIPT_NOT_FOUND,
        "ReceiptDetailNoPIIView having receiptId %d and operatorExternalUserId %s not found".formatted(
          receiptId, operatorExternalUserId)));
    return receiptDetailPIIViewMapper.map(receiptDetailNoPIIView);
  }

  @Override
  public ReceiptDetailDTO getReceiptDetail(Long receiptId, Long organizationId, String iud) {
    ReceiptDetailNoPIIView receiptDetailNoPIIView = receiptDetailNoPIIViewRepository.findReceiptDetailView(receiptId, organizationId, iud)
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_RECEIPT_NOT_FOUND,
        "ReceiptDetailNoPIIView having receiptId %d not found".formatted(
          receiptId)));
    return receiptDetailPIIViewMapper.map(receiptDetailNoPIIView);
  }
}
