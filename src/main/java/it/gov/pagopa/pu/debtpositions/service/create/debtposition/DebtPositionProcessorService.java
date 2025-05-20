package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

public interface DebtPositionProcessorService {
  void updateAmounts(DebtPositionDTO debtPositionDTO);
  void updateAmounts(DebtPosition debtPosition);
}
