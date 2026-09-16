package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeOrgOperatorsApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.RelateUserToDefaultDPTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeOrgOperatorsFacadeService;
import it.gov.pagopa.pu.debtpositions.service.DefaultDpTypeOrgOperatorFacadeService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class DebtPositionTypeOrgOperatorsController implements DebtPositionTypeOrgOperatorsApi {
  private final DebtPositionTypeOrgOperatorsFacadeService debtPositionTypeOrgOperatorsFacadeService;
  private final DefaultDpTypeOrgOperatorFacadeService defaultDpTypeOrgOperatorFacadeService;

  public DebtPositionTypeOrgOperatorsController(DebtPositionTypeOrgOperatorsFacadeService debtPositionTypeOrgOperatorsFacadeService, DefaultDpTypeOrgOperatorFacadeService defaultDpTypeOrgOperatorFacadeService) {
    this.debtPositionTypeOrgOperatorsFacadeService = debtPositionTypeOrgOperatorsFacadeService;
    this.defaultDpTypeOrgOperatorFacadeService = defaultDpTypeOrgOperatorFacadeService;
  }

  @Override
  public ResponseEntity<Integer> deleteOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds) {
    log.info("User requested deleteOperators having debtPositionTypeOrgId {}", debtPositionTypeOrgId);
    return ResponseEntity.ok(debtPositionTypeOrgOperatorsFacadeService.deleteOperators(debtPositionTypeOrgId, externalOperatorUserIds));
  }

  @Override
  public ResponseEntity<Void> saveDebtPositionTypeOrgOperatorsForOperator(String operatorExternalUserId, Set<Long> debtPositionTypeOrgIds) {
    log.info("User requested saveDebtPositionTypeOrgOperatorsForOperator having operatorExternalUserId {} and debtPositionTypeOrgIds {}", operatorExternalUserId, debtPositionTypeOrgIds);
    debtPositionTypeOrgOperatorsFacadeService.saveDebtPositionTypeOrgOperatorsForOperator(operatorExternalUserId, debtPositionTypeOrgIds);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  public ResponseEntity<Void> relateUserToDefaultDPTypeOrg(RelateUserToDefaultDPTypeOrgDTO relateUserToDefaultDPTypeOrgDTO) {
    log.info("User requested to grant default tech dp types to operatorExternalUserId {} on organizationId {}", relateUserToDefaultDPTypeOrgDTO.getOperatorExternalUserId(), relateUserToDefaultDPTypeOrgDTO.getOrganizationId());
    List<DebtPositionTypeOrgOperators> defaultTechDebtPositionTypeOrgOperators =
      defaultDpTypeOrgOperatorFacadeService.relateUserToDefaultDPTypeOrg(relateUserToDefaultDPTypeOrgDTO, SecurityUtils.getAccessToken());
    if(defaultTechDebtPositionTypeOrgOperators.isEmpty()) {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
