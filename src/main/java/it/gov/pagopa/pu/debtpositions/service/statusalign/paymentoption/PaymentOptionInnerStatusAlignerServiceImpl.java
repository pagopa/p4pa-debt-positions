package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;

public class PaymentOptionInnerStatusAlignerServiceImpl extends PaymentOptionStatusChecker implements PaymentOptionInnerStatusAlignerService{

  private final PaymentOptionRepository paymentOptionRepository;

  public PaymentOptionInnerStatusAlignerServiceImpl(PaymentOptionRepository paymentOptionRepository) {
    this.paymentOptionRepository = paymentOptionRepository;
  }

  @Override
  public void updatePaymentOptionStatus(PaymentOption paymentOption) {
    updateEntityStatus(
      paymentOption,
      po -> po.getInstallments().stream().map(InstallmentNoPII::getStatus).toList(),
      this::determinePaymentOptionStatus,
      PaymentOption::setStatus,
      (po, newStatus) -> paymentOptionRepository.updateStatus(po.getPaymentOptionId(), newStatus)
    );
  }
}
