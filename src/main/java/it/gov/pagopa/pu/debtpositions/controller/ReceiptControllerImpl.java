package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
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
    ReceiptDTO body = receiptService.createReceipt(receiptDTO);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }
}
