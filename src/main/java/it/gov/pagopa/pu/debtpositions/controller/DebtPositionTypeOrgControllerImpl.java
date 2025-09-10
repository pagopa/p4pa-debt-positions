package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.SaveDebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.DebtPositionTypeOrgTechHandlerService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebtPositionTypeOrgControllerImpl implements DebtPositionTypeOrgApi {
  private final DebtPositionTypeOrgService debtPositionTypeOrgService;
  private final DebtPositionTypeOrgTechHandlerService debtPositionTypeOrgTechHandlerService;
  private final MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService;

  public DebtPositionTypeOrgControllerImpl(
    DebtPositionTypeOrgService debtPositionTypeOrgService,
    DebtPositionTypeOrgTechHandlerService debtPositionTypeOrgTechHandlerService,
    MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService
  ) {
    this.debtPositionTypeOrgService = debtPositionTypeOrgService;
    this.debtPositionTypeOrgTechHandlerService = debtPositionTypeOrgTechHandlerService;
    this.mixedDebtPositionTypeOrgRetrieverService = mixedDebtPositionTypeOrgRetrieverService;
  }

  @Override
  public ResponseEntity<IONotificationDTO> getIONotificationDetails(Long debtPositionTypeOrgId, PaymentEventType paymentEventType) {
    return ResponseEntity.ok(debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, paymentEventType));
  }

  @Override
  public ResponseEntity<Void> createTechnicalDebtPositionTypeOrg(
    Long organizationId) {
    debtPositionTypeOrgTechHandlerService.createTechnicalDebtPositionTypeOrg(organizationId);
    mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(organizationId);
    return ResponseEntity.ok().build();
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
