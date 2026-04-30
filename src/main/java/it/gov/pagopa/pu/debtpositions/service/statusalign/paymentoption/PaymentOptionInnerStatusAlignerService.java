package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.dto.BasePaymentOption;

public interface PaymentOptionInnerStatusAlignerService {

  void updatePaymentOptionStatus(BasePaymentOption paymentOption);
}
