package it.gov.pagopa.pu.debtpositions.connector.migration.service;

import java.util.List;

public interface DebtPositionTypeOrgOperatorsService {
  List<Long> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode, String orgIpaCode);
  void consumeDebtPositionTypeOrgOperators(Long organizationId, List<Long> debtPositionTypeOrgIds, String fiscalCode, String orgIpaCode);
}
