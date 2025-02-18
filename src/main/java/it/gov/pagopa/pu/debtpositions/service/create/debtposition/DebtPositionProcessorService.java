package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;

public interface DebtPositionProcessorService {
  DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO);
  PaymentOptionDTO updatePaymentOptionAmounts(PaymentOptionDTO paymentOptionDTO);
}
