package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;

public interface DebtPositionTypeService {
  DebtPositionTypeDetailDTO getDebtPositionTypeDetail(Long debtPositionTypeId, Long brokerId, String accessToken);
}
