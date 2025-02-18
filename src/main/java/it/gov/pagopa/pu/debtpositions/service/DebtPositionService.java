package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.organization.dto.generated.Organization;

public interface DebtPositionService {

  DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO, Organization org);
}
