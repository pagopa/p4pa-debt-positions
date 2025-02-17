package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.CreateReceiptService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiptControllerImpl implements ReceiptApi {

  private final CreateReceiptService createReceiptService;
  private final ReceiptService receiptService;

  public ReceiptControllerImpl(CreateReceiptService createReceiptService, ReceiptService receiptService) {
    this.createReceiptService = createReceiptService;
    this.receiptService = receiptService;
  }

  @Override
  public ResponseEntity<ReceiptDTO> createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    String accessToken = SecurityUtils.getAccessToken();
    ReceiptDTO body = createReceiptService.createReceipt(receiptDTO, accessToken);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ReceiptDetailDTO> getReceiptDetail(Long receiptId, String operatorExternalUserId) {
    return ResponseEntity.ok(receiptService.getReceiptDetail(receiptId, operatorExternalUserId));
  }
}
