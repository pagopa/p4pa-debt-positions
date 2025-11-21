package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionEntityExtendedControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class DebtPositionEntityExtendedControllerImpl implements DebtPositionEntityExtendedControllerApi {

  private final DebtPositionRepository repository;

  public DebtPositionEntityExtendedControllerImpl(DebtPositionRepository repository) {
    this.repository = repository;
  }

  @Override
  public ResponseEntity<BaseDebtPosition> getDebtPositionNoPiiById(Long debtPositionId) {
    log.info("Requested DebtPosition no PII having id {}", debtPositionId);
    return ResponseEntity.ok(repository.findEntityGraphByDebtPositionId(debtPositionId));
  }

  @Override
  public ResponseEntity<List<BaseDebtPosition>> getDebtPositionsNoPiiByOrganizationIdAndInstallmentIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigins) {
    log.info("Requested DebtPositions no PII having orgId {}, iuv {} and dpOrigins {}", organizationId, iuv, debtPositionOrigins);
    List<DebtPosition> result = repository.findEntityGraphByOrganizationIdAndInstallmentIuv(organizationId, iuv, debtPositionOrigins);
    return ResponseEntity.ok(new ArrayList<>(result));
  }
}
