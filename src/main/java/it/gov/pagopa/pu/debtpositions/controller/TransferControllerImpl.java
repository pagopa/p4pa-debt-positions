package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.TransferApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static it.gov.pagopa.pu.debtpositions.controller.DebtPositionControllerImpl.HEADER_X_WORKFLOW_ID;

@RestController
@Slf4j
public class TransferControllerImpl implements TransferApi {

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;

  public TransferControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> notifyReportedTransferId(Long transferId){
    log.info("Reported transfer {}", transferId);
    Pair<DebtPositionDTO, String> result = debtPositionHierarchyStatusAlignerService.notifyReportedTransferId(transferId, SecurityUtils.getAccessToken());
    ResponseEntity.BodyBuilder outBuilder = ResponseEntity.status(HttpStatus.OK);
    if(StringUtils.isNotEmpty(result.getRight())){
      outBuilder.header(HEADER_X_WORKFLOW_ID, result.getRight());
    }
    return outBuilder.body(result.getLeft());
  }
}
