package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ModificationDebtPositionServiceImpl implements ModificationDebtPositionService {


  @Override
  public String modifyDebtPosition(DebtPositionDTO debtPositionDTO, InstallmentDTO installmentDTO) {
    return "";
  }
}
