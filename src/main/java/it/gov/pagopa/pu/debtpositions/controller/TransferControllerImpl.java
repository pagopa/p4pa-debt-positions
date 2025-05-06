package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.TransferApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferReportedRequest;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static it.gov.pagopa.pu.debtpositions.controller.DebtPositionControllerImpl.HEADER_X_RUN_ID;
import static it.gov.pagopa.pu.debtpositions.controller.DebtPositionControllerImpl.HEADER_X_WORKFLOW_ID;

@RestController
@Slf4j
public class TransferControllerImpl implements TransferApi {

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  public TransferControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
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
}
