package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentOptionInnerStatusAlignerServiceImpl extends PaymentOptionStatusChecker implements PaymentOptionInnerStatusAlignerService{


  public PaymentOptionInnerStatusAlignerServiceImpl(PaymentOptionRepository paymentOptionRepository) {
    super(paymentOptionRepository);
  }

  @Override
  public void updatePaymentOptionStatus(PaymentOption paymentOption) {
    updateEntityStatus(paymentOption);
  }
}
