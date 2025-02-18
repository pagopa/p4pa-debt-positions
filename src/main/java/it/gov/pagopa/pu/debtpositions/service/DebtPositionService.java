package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

public interface DebtPositionService {

  DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO);

  InstallmentDTO saveSingleInstallment(InstallmentDTO installmentDTO);
}
