package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebtPositionTypeOrgControllerImpl implements DebtPositionTypeOrgApi {
  private final DebtPositionTypeOrgService debtPositionTypeOrgService;

  public DebtPositionTypeOrgControllerImpl(DebtPositionTypeOrgService debtPositionTypeOrgService) {
    this.debtPositionTypeOrgService = debtPositionTypeOrgService;
  }

  @Override
  public ResponseEntity<DebtPositionTypeOrgDTO> findDebtPositionTypeOrgByOrganizationIdAndCode(Long organizationId, String code) {
    return ResponseEntity.ok(debtPositionTypeOrgService.getDebtPositionTypeOrgByOrganizationIdAndCode(organizationId, code));
  }

}
