package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.TransferApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PostalIbanVerifyResponse;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferReportedRequest;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import it.gov.pagopa.pu.debtpositions.service.TransferService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.controller.DebtPositionControllerImpl.HEADER_X_RUN_ID;
import static it.gov.pagopa.pu.debtpositions.controller.DebtPositionControllerImpl.HEADER_X_WORKFLOW_ID;

@RestController
@Slf4j
public class TransferControllerImpl implements TransferApi {

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final TaxonomyValidatorService taxonomyValidatorService;
  private final TransferService transferService;

  public TransferControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService,
                                TaxonomyValidatorService taxonomyValidatorService, TransferService transferService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.taxonomyValidatorService = taxonomyValidatorService;
    this.transferService = transferService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> notifyReportedTransferId(Long transferId, TransferReportedRequest transferReportedRequest){
    log.info("Reported transfer {}", transferId);
    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = debtPositionHierarchyStatusAlignerService.notifyReportedTransferId(transferId, transferReportedRequest, SecurityUtils.getAccessToken());
    ResponseEntity.BodyBuilder outBuilder = ResponseEntity.status(HttpStatus.OK);
    if(result.getRight() != null){
      outBuilder.header(HEADER_X_WORKFLOW_ID, result.getRight().getWorkflowId());
      outBuilder.header(HEADER_X_RUN_ID, result.getRight().getRunId());
    }
    return outBuilder.body(result.getLeft());
  }

  @Override
  public ResponseEntity<Boolean> validateTaxonomyCategory(String taxonomyCategory, String orgFiscalCode) {
    log.info("User requested validation on taxonomyCategory [{}] and orgFiscalCode", taxonomyCategory);
    return ResponseEntity.ok(taxonomyValidatorService.validateTaxonomyCategory(taxonomyCategory, orgFiscalCode));
  }

  @Override
  public ResponseEntity<PostalIbanVerifyResponse> verifyPostalIban(List<Long> installmentIds) {
    log.info("User requested verifyPostalIban with installmentIds {}", installmentIds);
    return ResponseEntity.ofNullable(transferService.verifyPostalIban(installmentIds));
  }
}
