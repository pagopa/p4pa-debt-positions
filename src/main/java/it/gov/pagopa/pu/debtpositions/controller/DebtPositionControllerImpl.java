package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentServiceImpl;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebtPositionControllerImpl implements DebtPositionApi {

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final DebtPositionCreationService debtPositionCreationService;
  private final DebtPositionService debtPositionService;
  private final InstallmentSynchronizeService installmentSynchronizeService;
  private final DebtPositionUpdateInstallmentServiceImpl debtPositionUpdateInstallmentService;

  public DebtPositionControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionCreationService debtPositionCreationService, DebtPositionService debtPositionService, InstallmentSynchronizeService installmentSynchronizeService, DebtPositionUpdateInstallmentServiceImpl debtPositionUpdateInstallmentService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionService = debtPositionService;
    this.installmentSynchronizeService = installmentSynchronizeService;
    this.debtPositionUpdateInstallmentService = debtPositionUpdateInstallmentService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(false)
      .build();
    DebtPositionDTO body = debtPositionCreationService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId).getLeft();
    return new ResponseEntity<>(body, HttpStatus.OK);
  }


  @Override
  public ResponseEntity<DebtPositionDTO> finalizeSyncStatus(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> requestBody) {
    DebtPositionDTO body = debtPositionHierarchyStatusAlignerService.finalizeSyncStatus(debtPositionId, requestBody);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<DebtPositionDTO> checkAndUpdateInstallmentExpiration(Long debtPositionId) {
    DebtPositionDTO body = debtPositionHierarchyStatusAlignerService.checkAndUpdateInstallmentExpiration(debtPositionId);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<DebtPositionDTO> getDebtPosition(Long debtPositionId) {
    return ResponseEntity.ok(debtPositionService.getDebtPosition(debtPositionId));
  }

  @Override
  public ResponseEntity<Void> installmentSynchronize(DebtPositionOrigin origin, InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive, Boolean partialChange){
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(partialChange)
      .executionConfig(installmentSynchronizeDTO.getExecutionConfig())
      .build();
    String workflowId = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, origin, accessToken, operatorExternalUserId);
    return ResponseEntity.status(HttpStatus.CREATED).header("x-workflow-id", workflowId).build();
  }

  @Override
  public ResponseEntity<Void> updateInstallment(UpdateInstallmentRequest updateInstallmentRequest) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();
    String workflowId = debtPositionUpdateInstallmentService.updateInstallment(updateInstallmentRequest.getDebtPositionDTO(), updateInstallmentRequest.getInstallments2operate(), wfExecutionParameters, accessToken, operatorExternalUserId).getRight();
    return ResponseEntity.status(HttpStatus.CREATED).header("x-workflow-id", workflowId).build();
  }

  @Override
  public ResponseEntity<PagedDebtPositions> getDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable) {
    return ResponseEntity.ok(debtPositionService.getPagedDebtPositionsByIngestionFlowFileId(ingestionFlowFileId, pageable));
  }
}

