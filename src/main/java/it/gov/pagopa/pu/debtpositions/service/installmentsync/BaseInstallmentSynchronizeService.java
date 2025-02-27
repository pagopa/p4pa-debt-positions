package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

public interface BaseInstallmentSynchronizeService {

  InstallmentDTO findInstallment(DebtPositionDTO debtPositionDTO, String iud, Integer paymentOptionIndex);
}
