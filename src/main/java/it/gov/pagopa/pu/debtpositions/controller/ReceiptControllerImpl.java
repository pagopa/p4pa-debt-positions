package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.controller.generated.ReceiptApi;
import it.gov.pagopa.pu.debtpositions.dto.FileResourceDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.ReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.CreateReceiptService;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.ReceiptFileService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiptControllerImpl implements ReceiptApi {

  private final CreateReceiptService createReceiptService;
  private final ReceiptService receiptService;
  private final ReceiptFileService receiptFileService;
  private final OrganizationService organizationService;

  public ReceiptControllerImpl(CreateReceiptService createReceiptService, ReceiptService receiptService, ReceiptFileService receiptFileService, OrganizationService organizationService) {
    this.createReceiptService = createReceiptService;
    this.receiptService = receiptService;
    this.receiptFileService = receiptFileService;
    this.organizationService = organizationService;
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
  public ResponseEntity<ReceiptDetailDTO> getReceiptDetail(Long receiptId, String operatorExternalUserId, Long organizationId) {
    return ResponseEntity.ok(receiptService.getReceiptDetail(receiptId, operatorExternalUserId, organizationId));
  }

  @Override
  public ResponseEntity<Resource> getReceiptPdf(Long receiptId, Long organizationId) {
    ReceiptDetailDTO receiptDetail = receiptService.getReceiptDetail(receiptId, SecurityUtils.getCurrentUserExternalId(), organizationId);
    if(receiptDetail==null){
      throw new NotFoundException("Receipt with ID "+receiptId+" not found");
    }
    Organization organization = organizationService.getOrganizationById(organizationId, SecurityUtils.getAccessToken())
      .orElseThrow(() -> new NotFoundException("Organization with ID "+organizationId+" not found"));

    FileResourceDTO fileResourceDTO = new FileResourceDTO(
      new ByteArrayResource(receiptFileService.generateReceiptPdf(receiptDetail, organization)),
      "RECEIPT_"+organization.getOrgFiscalCode()+"_"+receiptId+".pdf"
    );

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
