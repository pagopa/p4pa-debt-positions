package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.StatusRulesHandler;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus.TO_SYNC;

public class PaymentOptionStatusChecker extends StatusRulesHandler<InstallmentStatus, PaymentOption, PaymentOptionStatus> {

  private final PaymentOptionRepository paymentOptionRepository;

  public PaymentOptionStatusChecker(PaymentOptionRepository paymentOptionRepository) {
    super(InstallmentStatus.TO_SYNC, InstallmentStatus.PAID, InstallmentStatus.UNPAID,
      InstallmentStatus.EXPIRED, InstallmentStatus.CANCELLED, InstallmentStatus.REPORTED);
    this.paymentOptionRepository = paymentOptionRepository;
  }

  @Override
  public PaymentOptionStatus calculateNewStatus(List<InstallmentStatus> installmentStatusList) {
    if (isToSync(installmentStatusList)) {
      return TO_SYNC;
    } else if (isPartiallyPaid(installmentStatusList)) {
      return PaymentOptionStatus.PARTIALLY_PAID;
    } else if (isUnpaid(installmentStatusList)) {
      return PaymentOptionStatus.UNPAID;
    } else if (isPaid(installmentStatusList)) {
      return PaymentOptionStatus.PAID;
    } else if (isReported(installmentStatusList)) {
      return PaymentOptionStatus.REPORTED;
    } else if (isInvalid(installmentStatusList)) {
      return PaymentOptionStatus.INVALID;
    } else if (isCancelled(installmentStatusList)) {
      return PaymentOptionStatus.CANCELLED;
    } else if (isExpired(installmentStatusList)) {
      return PaymentOptionStatus.EXPIRED;
    } else {
      throw new InvalidValueException("Unable to determine status for PaymentOption");
    }
  }

  @Override
  protected List<InstallmentStatus> getChildStatuses(PaymentOption paymentOption) {
    return paymentOption.getInstallments().stream()
      .map(InstallmentNoPII::getStatus)
      .toList();
  }

  @Override
  protected void setStatus(PaymentOption paymentOption, PaymentOptionStatus newStatus) {
    paymentOption.setStatus(newStatus);
  }

  @Override
  protected void storeStatus(PaymentOption paymentOption, PaymentOptionStatus newStatus) {
    paymentOptionRepository.updateStatus(paymentOption.getPaymentOptionId(), newStatus);
  }

  @Override
  protected boolean isPartiallyPaid(List<InstallmentStatus> childrenStatusList) {
    return childrenStatusList.contains(InstallmentStatus.PAID) &&
      (childrenStatusList.contains(InstallmentStatus.UNPAID) || childrenStatusList.contains(InstallmentStatus.EXPIRED));
  }

  public boolean isInvalid(List<InstallmentStatus> childrenStatusList) {
    return childrenStatusList.contains(InstallmentStatus.INVALID) &&
      childrenStatusList.stream().allMatch(status -> InstallmentStatus.INVALID.equals(status) || InstallmentStatus.CANCELLED.equals(status));
  }
}
