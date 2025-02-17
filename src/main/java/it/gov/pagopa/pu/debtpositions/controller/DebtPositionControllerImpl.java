package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.CreateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.DebtPositionHierarchyStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebtPositionControllerImpl implements DebtPositionApi {

  private final DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService;
  private final CreateDebtPositionService createDebtPositionService;

  public DebtPositionControllerImpl(DebtPositionHierarchyStatusAlignerService debtPositionHierarchyStatusAlignerService, CreateDebtPositionService createDebtPositionService) {
    this.debtPositionHierarchyStatusAlignerService = debtPositionHierarchyStatusAlignerService;
    this.createDebtPositionService = createDebtPositionService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive) {
    String accessToken = SecurityUtils.getAccessToken();
    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    DebtPositionDTO body = createDebtPositionService.createDebtPosition(debtPositionDTO, massive, accessToken, operatorExternalUserId);
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
}

