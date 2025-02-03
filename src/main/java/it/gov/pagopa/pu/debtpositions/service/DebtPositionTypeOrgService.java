package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;

public interface DebtPositionTypeOrgService {
  DebtPositionTypeOrgDTO getDebtPositionTypeOrgByOrganizationIdAndCode(Long organizationId, String code);
}
