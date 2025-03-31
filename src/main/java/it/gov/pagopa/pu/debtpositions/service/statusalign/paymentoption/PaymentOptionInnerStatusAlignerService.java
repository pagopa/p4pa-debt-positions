package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.model.PaymentOption;

public interface PaymentOptionInnerStatusAlignerService {

  void updatePaymentOptionStatus(PaymentOption paymentOption);
}
