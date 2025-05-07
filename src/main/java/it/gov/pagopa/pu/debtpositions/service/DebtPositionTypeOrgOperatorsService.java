package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import java.util.List;
import java.util.Set;

public interface DebtPositionTypeOrgOperatorsService {
  long deleteOperatorsByDebtPositionTypeOrgId(Long debtPositionTypeOrgId);
  List<DebtPositionTypeOrgOperators> saveOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds);
  int deleteOperators(Long debtPositionTypeOrgId, Set<String> externalOperatorUserIds);
}
