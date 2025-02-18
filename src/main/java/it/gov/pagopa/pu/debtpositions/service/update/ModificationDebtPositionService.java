package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

public interface ModificationDebtPositionService {

  String modifyDebtPosition(DebtPositionDTO debtPositionDTO, InstallmentDTO installmentDTO);
}
