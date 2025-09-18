package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgOperatorsApi;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgOperatorsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class DebtPositionTypeOrgOperatorsController implements DebtPositionTypeOrgOperatorsApi {
  private final DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;

  public DebtPositionTypeOrgOperatorsController(DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService) {
    this.debtPositionTypeOrgOperatorsService = debtPositionTypeOrgOperatorsService;
  }

  @Override
  public ResponseEntity<Integer> deleteOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds) {
    log.info("User requested deleteOperators having debtPositionTypeOrgId {}", debtPositionTypeOrgId);
    return ResponseEntity.ok(debtPositionTypeOrgOperatorsService.deleteOperators(debtPositionTypeOrgId, externalOperatorUserIds));
  }

  @Override
  public ResponseEntity<List<DebtPositionTypeOrgOperators>> saveDebtPositionTypeOrgOperatorsForOperator(String operatorExternalUserId, Set<Long> debtPositionTypeOrgIds) {
    log.info("User requested saveDebtPositionTypeOrgOperatorsForOperator having operatorExternalUserId {} and debtPositionTypeOrgIds {}", operatorExternalUserId, debtPositionTypeOrgIds);
    return ResponseEntity.ok(debtPositionTypeOrgOperatorsService.saveDebtPositionTypeOrgOperatorsForOperator(operatorExternalUserId, debtPositionTypeOrgIds));
  }
}
