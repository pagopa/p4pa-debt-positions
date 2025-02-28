package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.data.util.Pair;

public interface DebtPositionService {

  Pair<DebtPosition, DebtPositionDTO> saveDebtPosition(DebtPositionDTO debtPositionDTO, Organization org);
  DebtPositionDTO getDebtPosition(Long debtPositionId);
}
