package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;

public interface DebtPositionProcessorService {
  DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO);
  void updatePaymentOptionAmounts(PaymentOptionDTO paymentOptionDTO);

  void synchronizeAmountsAndStatus(DebtPositionDTO debtPositionDTO, InstallmentDTO installmentDTO);
}
