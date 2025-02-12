package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiptControllerImpl implements ReceiptApi {

  private final ReceiptService receiptService;

  public ReceiptControllerImpl(ReceiptService receiptService) {
    this.receiptService = receiptService;
  }

  @Override
  public ResponseEntity<ReceiptDTO> createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    String accessToken = SecurityUtils.getAccessToken();
    ReceiptDTO body = receiptService.createReceipt(receiptDTO, accessToken);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ReceiptDTO> getReceiptDetail(Long receiptId) {
    return ResponseEntity.ok(receiptService.getReceiptDetail(receiptId));
  }
}
