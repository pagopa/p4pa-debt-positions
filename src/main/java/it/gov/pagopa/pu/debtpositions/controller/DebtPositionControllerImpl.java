package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.InstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.springframework.http.HttpHeaders;
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

  public DebtPositionControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, DebtPositionCreationService debtPositionCreationService, DebtPositionService debtPositionService, InstallmentSynchronizeService installmentSynchronizeService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionService = debtPositionService;
    this.installmentSynchronizeService = installmentSynchronizeService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    DebtPositionDTO body = debtPositionCreationService.createDebtPosition(debtPositionDTO, massive, accessToken, operatorExternalUserId);
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
  public ResponseEntity<Void> installmentSynchronize(DebtPositionOrigin origin, InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive){
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    String workflowId = installmentSynchronizeService.installmentSynchronize(installmentSynchronizeDTO, massive, origin, accessToken, operatorExternalUserId);
    HttpHeaders headers = new HttpHeaders();
    headers.add("x-workflow-id", workflowId);
    return new ResponseEntity<>(headers, HttpStatus.CREATED);
  }
}

