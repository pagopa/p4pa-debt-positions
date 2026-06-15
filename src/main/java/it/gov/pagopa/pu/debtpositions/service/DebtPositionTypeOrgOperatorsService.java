package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import java.util.List;
import java.util.Set;

public interface DebtPositionTypeOrgOperatorsService {
  int deleteOperatorsByDebtPositionTypeOrgId(Long debtPositionTypeOrgId);
  List<DebtPositionTypeOrgOperators> saveOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds);
  int deleteOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds);
  List<DebtPositionTypeOrgOperators> saveDebtPositionTypeOrgOperatorsForOperator(String operatorExternalUserId, Set<Long> debtPositionTypeOrgIds);
  List<DebtPositionTypeOrgOperators> saveDefaultTechnicalDebtPositionTypeOrgOperatorsForOperator(String operatorExternalUserId, Long organizationId);
}
