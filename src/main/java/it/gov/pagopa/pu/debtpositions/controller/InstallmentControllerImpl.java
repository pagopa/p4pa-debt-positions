package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.InstallmentApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InstallmentControllerImpl implements InstallmentApi {
  private final InstallmentService installmentService;

  public InstallmentControllerImpl(InstallmentService installmentService) {
    this.installmentService = installmentService;
  }

  @Override
  public ResponseEntity<List<InstallmentDTO>> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav,List<DebtPositionOrigin> debtPositionOrigin) {
    return ResponseEntity.ok(installmentService.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin));
  }
}
