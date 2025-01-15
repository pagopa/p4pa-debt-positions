package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DebtPositionControllerImpl implements DebtPositionApi {
  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO) {
    return DebtPositionApi.super.createDebtPosition(debtPositionDTO);
  }

  @Override
  public ResponseEntity<Void> finalizeSyncStatus(Long debtPositionId, Map<String, IudSyncStatusUpdateDTO> requestBody) {
    return DebtPositionApi.super.finalizeSyncStatus(debtPositionId, requestBody);
  }
}
