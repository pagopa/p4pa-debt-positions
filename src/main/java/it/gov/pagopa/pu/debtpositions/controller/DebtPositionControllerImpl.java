package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class DebtPositionControllerImpl implements DebtPositionApi {

  public static final String HEADER_X_WORKFLOW_ID = "x-workflow-id";

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final DebtPositionCreationService debtPositionCreationService;
  private final DebtPositionService debtPositionService;
  private final InstallmentSynchronizeService installmentSynchronizeService;
  private final InstallmentService installmentService;

  public DebtPositionControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionCreationService debtPositionCreationService, DebtPositionService debtPositionService, InstallmentSynchronizeService installmentSynchronizeService, InstallmentService installmentService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionService = debtPositionService;
    this.installmentSynchronizeService = installmentSynchronizeService;
    this.installmentService = installmentService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    WfExecutionParameters wfExecutionParameters = WfExecutionParameters.builder()
      .massive(massive)
      .partialChange(false)
      .build();
    Pair<DebtPositionDTO, String> result = debtPositionCreationService.createDebtPosition(debtPositionDTO, wfExecutionParameters, accessToken, operatorExternalUserId);
    return ResponseEntity.status(HttpStatus.OK).header(HEADER_X_WORKFLOW_ID, result.getRight()).body(result.getLeft());
  }


  @Override
  public ResponseEntity<DebtPositionDTO> finalizeSyncStatus(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> requestBody) {
    log.info("Finalizing debtPosition TO_SYNC installment status on debtPosition {}: {}", debtPositionId, requestBody);
    DebtPositionDTO body = debtPositionHierarchyStatusAlignerService.finalizeSyncStatus(debtPositionId, requestBody);
    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<DebtPositionDTO> checkAndUpdateInstallmentExpiration(Long debtPositionId) {
    log.info("Checking installment expired on debtPosition {}", debtPositionId);
    Pair<DebtPositionDTO, String> result = debtPositionHierarchyStatusAlignerService.checkAndUpdateInstallmentExpiration(debtPositionId, SecurityUtils.getAccessToken());
    return ResponseEntity.status(HttpStatus.OK).header(HEADER_X_WORKFLOW_ID, result.getRight()).body(result.getLeft());
  }

  @Override
  public ResponseEntity<DebtPositionDTO> getDebtPosition(Long debtPositionId) {
    log.info("Retrieving DebtPosition {}", debtPositionId);
    return ResponseEntity.ok(debtPositionService.getDebtPosition(debtPositionId));
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
    String workflowId = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, wfExecutionParameters, origin, accessToken, operatorExternalUserId);
    return ResponseEntity.status(HttpStatus.CREATED).header(HEADER_X_WORKFLOW_ID, workflowId).build();
  }

  @Override
  public ResponseEntity<PagedDebtPositions> getDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, Pageable pageable) {
    log.info("Retrieving debtPositions related to ingestionFlowFile {} (pageNumber: {})", ingestionFlowFileId, pageable.getPageNumber());
    return ResponseEntity.ok(debtPositionService.getPagedDebtPositionsByIngestionFlowFileId(ingestionFlowFileId, pageable));
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

    String workflowId = installmentService.updateInstallmentNotificationDate(updateInstallmentNotificationDateRequest, wfExecutionParameters, operatorExternalUserId, accessToken);
    return ResponseEntity.status(HttpStatus.CREATED).header(HEADER_X_WORKFLOW_ID, workflowId).build();
  }

  @Override
  public ResponseEntity<InstallmentDTO> updateInstallmentNotificationFee(UpdateInstallmentNotificationFeeRequest updateInstallmentNotificationFeeRequest) {
    log.info("Updating notification fee on installment having NAV {} and OrganizationId {}",
      updateInstallmentNotificationFeeRequest.getNav(), updateInstallmentNotificationFeeRequest.getOrganizationId());

    InstallmentDTO installmentDTO = installmentService.updateInstallmentNotificationFee(
      updateInstallmentNotificationFeeRequest.getOrganizationId(),
      updateInstallmentNotificationFeeRequest.getNav(),
      updateInstallmentNotificationFeeRequest.getDebtPositionOrigin(),
      updateInstallmentNotificationFeeRequest.getNewFee());

    return ResponseEntity.ok(installmentDTO);
  }
}

