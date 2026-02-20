package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.CreateReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.ReceiptFileService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ReceiptControllerImpl implements ReceiptApi {

  private final CreateReceiptService createReceiptService;
  private final ReceiptService receiptService;
  private final ReceiptFileService receiptFileService;

  public ReceiptControllerImpl(CreateReceiptService createReceiptService, ReceiptService receiptService, ReceiptFileService receiptFileService) {
    this.createReceiptService = createReceiptService;
    this.receiptService = receiptService;
    this.receiptFileService = receiptFileService;
  }

  @Override
  public ResponseEntity<ReceiptDTO> createReceipt(ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    String accessToken = SecurityUtils.getAccessToken();
    ReceiptDTO body = createReceiptService.createReceipt(receiptDTO, accessToken);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ReceiptDTO> getReceipt(Long receiptId) {
    return ResponseEntity.ok(receiptService.getReceipt(receiptId));
  }

  @Override
  public ResponseEntity<ReceiptDetailDTO> getReceiptDetail(Long receiptId, Long organizationId, String operatorExternalUserId, String iud) {
    return ResponseEntity.ok(receiptService.getReceiptDetail(receiptId, operatorExternalUserId, organizationId, iud));
  }

  @Override
  public ResponseEntity<Resource> getReceiptPdf(Long receiptId, Long organizationId) {
    log.info("Request receipt pdf for receiptId {} and organizationId {}", receiptId, organizationId);

    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();

    FileResourceDTO fileResourceDTO = receiptFileService.generateReceiptPdf(accessToken, operatorExternalUserId, receiptId, organizationId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(ContentDisposition.attachment()
      .filename(fileResourceDTO.getFileName())
      .build());

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_PDF)
      .headers(headers)
      .body(fileResourceDTO.getResource());
  }
}
