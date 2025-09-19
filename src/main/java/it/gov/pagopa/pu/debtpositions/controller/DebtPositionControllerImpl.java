package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.MixedDebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.delete.DebtPositionDeletionService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.PublishDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionManageInstallmentsService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class DebtPositionControllerImpl implements DebtPositionApi {

  public static final String HEADER_X_WORKFLOW_ID = "x-workflow-id";
  public static final String HEADER_X_RUN_ID = "x-run-id";

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final DebtPositionCreationService debtPositionCreationService;
  private final DebtPositionService debtPositionService;
  private final InstallmentSynchronizeService installmentSynchronizeService;
  private final InstallmentService installmentService;
  private final DebtPositionManageInstallmentsService debtPositionManageService;
  private final DebtPositionDeletionService debtPositionDeletionService;
  private final PublishDebtPositionService publishDebtPositionService;
  private final MixedDebtPositionCreationService mixedDebtPositionCreationService;

  public DebtPositionControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionCreationService debtPositionCreationService, DebtPositionService debtPositionService, InstallmentSynchronizeService installmentSynchronizeService, InstallmentService installmentService, DebtPositionManageInstallmentsService debtPositionManageService, DebtPositionDeletionService debtPositionDeletionService, PublishDebtPositionService publishDebtPositionService,
    MixedDebtPositionCreationService mixedDebtPositionCreationService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionService = debtPositionService;
    this.installmentSynchronizeService = installmentSynchronizeService;
    this.installmentService = installmentService;
    this.debtPositionManageService = debtPositionManageService;
    this.debtPositionDeletionService = debtPositionDeletionService;
    this.publishDebtPositionService = publishDebtPositionService;
    this.mixedDebtPositionCreationService = mixedDebtPositionCreationService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(false)
      .build();
    WorkflowCreatedDTO workflow = debtPositionCreationService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
    if(workflow != null) {
      return ResponseEntity
        .status(HttpStatus.OK)
        .header(HEADER_X_WORKFLOW_ID, workflow.getWorkflowId())
        .header(HEADER_X_RUN_ID, workflow.getRunId())
        .body(debtPositionDTO);
    } else {
      return ResponseEntity
        .status(HttpStatus.OK)
        .body(debtPositionDTO);
    }
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createMixedDebtPosition(
    MixedDebtPositionDTO mixedDebtPositionDTO) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();

    Pair<WorkflowCreatedDTO, DebtPositionDTO> result = mixedDebtPositionCreationService.createMixedDebtPosition(
      mixedDebtPositionDTO, accessToken, operatorExternalUserId);
    WorkflowCreatedDTO workflow = result.getLeft();
    DebtPositionDTO debtPositionDTO = result.getRight();

    if (workflow != null) {
      return ResponseEntity
        .status(HttpStatus.OK)
        .header(HEADER_X_WORKFLOW_ID, workflow.getWorkflowId())
        .header(HEADER_X_RUN_ID, workflow.getRunId())
        .body(debtPositionDTO);
    } else {
      return ResponseEntity
        .status(HttpStatus.OK)
        .body(debtPositionDTO);
    }
  }

  @Override
  public ResponseEntity<DebtPositionDTO> finalizeSyncStatus(Long debtPositionId, SyncStatusUpdateRequestDTO requestBody) {
    log.info("Finalizing debtPosition TO_SYNC installment status on debtPosition {}: {}", debtPositionId, requestBody);
    DebtPositionDTO body = debtPositionHierarchyStatusAlignerService.finalizeSyncStatus(debtPositionId, requestBody);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<DebtPositionDTO> checkAndUpdateInstallmentExpiration(Long debtPositionId) {
    log.info("Checking installment expired on debtPosition {}", debtPositionId);
    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = debtPositionHierarchyStatusAlignerService.checkAndUpdateInstallmentExpiration(debtPositionId, SecurityUtils.getAccessToken());
    return ResponseEntity
      .status(HttpStatus.OK)
      .header(HEADER_X_WORKFLOW_ID, result.getRight().getWorkflowId())
      .header(HEADER_X_RUN_ID, result.getRight().getRunId())
      .body(result.getLeft());
  }

  @Override
  public ResponseEntity<DebtPositionDTO> getDebtPosition(Long debtPositionId) {
    log.info("Retrieving DebtPosition {}", debtPositionId);
    return ResponseEntity.ok(debtPositionService.getDebtPosition(debtPositionId));
  }

  @Override
  public ResponseEntity<DebtPositionDTO> getDebtPositionByInstallmentId(Long installmentId) {
    log.info("Retrieving DebtPosition by installmentId {}", installmentId);
    return ResponseEntity.ok(debtPositionService.getDebtPositionByInstallmentId(installmentId));
  }

  @Override
  public ResponseEntity<List<DebtPositionDTO>> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin) {
    log.info("Retrieving DebtPosition by orgId[{}] and iud [{}]", organizationId, iud);
    return ResponseEntity.ok(debtPositionService.getDebtPositionsByOrganizationIdAndIud(organizationId, iud, debtPositionOrigin));
  }

  @Override
  public ResponseEntity<List<DebtPositionDTO>> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin) {
    log.info("Retrieving DebtPosition by orgId[{}] and iuv [{}]", organizationId, iuv);
    return ResponseEntity.ok(debtPositionService.getDebtPositionsByOrganizationIdAndIuv(organizationId, iuv, debtPositionOrigin));
  }

  @Override
  public ResponseEntity<Void> installmentSynchronize(DebtPositionOrigin origin, InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive, Boolean partialChange){
    log.info("Synchronizing installment having IUD {} of debtPosition having iupdOrg {} (origin: {}, organizationId: {})",
      installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getIupdOrg(), origin, installmentSynchronizeDTO.getOrganizationId());
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(partialChange)
      .executionConfig(installmentSynchronizeDTO.getExecutionConfig())
      .build();
    WorkflowCreatedDTO workflow = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, origin, accessToken, operatorExternalUserId);
    if(workflow != null) {
      return ResponseEntity
        .status(HttpStatus.OK)
        .header(HEADER_X_WORKFLOW_ID, workflow.getWorkflowId())
        .header(HEADER_X_RUN_ID, workflow.getRunId())
        .build();
    } else {
      return ResponseEntity
        .status(HttpStatus.OK)
        .build();
    }
  }

  @Override
  public ResponseEntity<PagedDebtPositions> getDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, List<InstallmentStatus> statusToExclude, Pageable pageable) {
    log.info("Retrieving debtPositions related to ingestionFlowFile {} (pageNumber: {})", ingestionFlowFileId, pageable.getPageNumber());
    return ResponseEntity.ok(debtPositionService.getPagedDebtPositionsByIngestionFlowFileId(ingestionFlowFileId, statusToExclude, pageable));
  }

  @Override
  public ResponseEntity<Void> updateInstallmentNotificationDate(UpdateInstallmentNotificationDateRequest updateInstallmentNotificationDateRequest) {
    log.info("Updating notificationDate on installment having NAV {} on DebtPosition {}: {}",
      updateInstallmentNotificationDateRequest.getNav(), updateInstallmentNotificationDateRequest.getDebtPositionId(), updateInstallmentNotificationDateRequest.getNotificationDate());
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    WorkflowCreatedDTO workflow = installmentService.updateInstallmentNotificationDate(updateInstallmentNotificationDateRequest, wfExecutionParameters, operatorExternalUserId, accessToken);
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .header(HEADER_X_WORKFLOW_ID, workflow.getWorkflowId())
      .header(HEADER_X_RUN_ID, workflow.getRunId())
      .build();
  }

  @Override
  public ResponseEntity<DebtPositionDTO> manageDebtPositionInstallments(Long debtPositionId, ManageDebtPositionDTO manageDebtPositionDTO){
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();
    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = debtPositionManageService.manageDebtPositionInstallments(debtPositionId, manageDebtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
    if(result.getRight() != null) {
      return ResponseEntity
        .status(HttpStatus.OK)
        .header(HEADER_X_WORKFLOW_ID, result.getRight().getWorkflowId())
        .header(HEADER_X_RUN_ID, result.getRight().getRunId())
        .body(result.getLeft());
    } else {
      return ResponseEntity
        .status(HttpStatus.OK)
        .body(result.getLeft());
    }
  }

  @Override
  public ResponseEntity<InstallmentDTO> updateInstallmentNotificationFee(ActualizeAmountRequestDTO actualizeAmountRequest) {
    log.info("Updating notification fee on installment having NAV {} and OrganizationId {}",
      actualizeAmountRequest.getNav(), actualizeAmountRequest.getOrganizationId());

    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    InstallmentDTO installmentDTO = installmentService.updateInstallmentNotificationFee(
      actualizeAmountRequest,
      wfExecutionParameters,
      accessToken,
      operatorExternalUserId);

    return ResponseEntity.ok(installmentDTO);
  }

  @Override
  public ResponseEntity<Void> deleteDebtPosition(Long debtPositionId){
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WorkflowCreatedDTO workflow = debtPositionDeletionService.deleteDebtPosition(debtPositionId, accessToken, operatorExternalUserId);
    if(workflow != null) {
      return ResponseEntity
        .status(HttpStatus.OK)
        .header(HEADER_X_WORKFLOW_ID, workflow.getWorkflowId())
        .header(HEADER_X_RUN_ID, workflow.getRunId())
        .build();
    } else {
      return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
    }
  }

  @Override
  public ResponseEntity<DebtPositionDTO> publishDebtPosition(Long debtPositionId) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(false)
      .partialChange(false)
      .build();

    log.info("Publishing debtPosition with id {}", debtPositionId);
    Pair<DebtPositionDTO, WorkflowCreatedDTO> result = publishDebtPositionService.publishDebtPosition(debtPositionId, wfExecutionParameters, accessToken, operatorExternalUserId);
    return ResponseEntity
      .status(HttpStatus.OK)
      .header(HEADER_X_WORKFLOW_ID, result.getRight().getWorkflowId())
      .header(HEADER_X_RUN_ID, result.getRight().getRunId())
      .body(result.getLeft());
  }
}
