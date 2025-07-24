package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebtPositionTypeOrgControllerImpl implements DebtPositionTypeOrgApi {
  private final DebtPositionTypeOrgService debtPositionTypeOrgService;

  public DebtPositionTypeOrgControllerImpl(DebtPositionTypeOrgService debtPositionTypeOrgService) {
    this.debtPositionTypeOrgService = debtPositionTypeOrgService;
  }

  @Override
  public ResponseEntity<IONotificationDTO> getIONotificationDetails(Long debtPositionTypeOrgId, PaymentEventType paymentEventType) {
    return ResponseEntity.ok(debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, paymentEventType));
  }

  @Override
  public ResponseEntity<Void> deleteDebtPositionTypeOrg(
    Long debtPositionTypeOrgId) {
    debtPositionTypeOrgService.deleteDebtPositionTypeOrg(debtPositionTypeOrgId);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<DebtPositionTypeOrg> saveDebtPositionTypeOrg(
    SaveDebtPositionTypeOrgDTO saveDebtPositionTypeOrgDTO) {
    return ResponseEntity.ok(debtPositionTypeOrgService.saveDebtPositionTypeOrg(saveDebtPositionTypeOrgDTO));
  }

  @Override
  public ResponseEntity<Void> updateFlagActiveDebtPositionTypeOrg(Long debtPositionTypeOrgId, Boolean flagActive) {
   debtPositionTypeOrgService.updateFlagActiveDebtPositionTypeOrg(debtPositionTypeOrgId, flagActive);
    return ResponseEntity.ok().build();
  }
}
