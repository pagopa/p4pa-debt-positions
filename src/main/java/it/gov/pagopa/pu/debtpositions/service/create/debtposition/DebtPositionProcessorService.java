package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

public interface DebtPositionProcessorService {
  DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO);
}
