package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.InstallmentApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.service.InstallmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class InstallmentControllerImpl implements InstallmentApi {
  private final InstallmentService installmentService;

  public InstallmentControllerImpl(InstallmentService installmentService) {
    this.installmentService = installmentService;
  }

  @Override
  public ResponseEntity<List<InstallmentDTO>> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return ResponseEntity.ok(installmentService.getInstallmentsByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin));
  }

  @Override
  public ResponseEntity<InstallmentDetailDTO> getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    return ResponseEntity.ok(installmentService.getInstallmentDetail(installmentId, operatorExternalUserId));
  }

  @Override
  public ResponseEntity<List<InstallmentDTO>> getInstallmentsByOrganizationIdAndReceiptId(Long organizationId, Long receiptId, List<DebtPositionOrigin> debtPositionOrigin) {
    log.info("Retrieve installments by organizationId {} and receiptId {} and debtPositionOrigin {}", organizationId, receiptId, debtPositionOrigin);
    return ResponseEntity.ok(installmentService.getInstallmentsByOrganizationIdAndReceiptId(organizationId, receiptId, debtPositionOrigin));
  }


}
