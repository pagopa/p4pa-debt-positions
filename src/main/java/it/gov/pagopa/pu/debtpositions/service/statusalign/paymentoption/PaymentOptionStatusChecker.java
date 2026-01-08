package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import static it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus.TO_SYNC;

import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.BasePaymentOption;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.StatusRulesHandler;
import java.util.List;

public class PaymentOptionStatusChecker extends StatusRulesHandler<InstallmentStatus, BasePaymentOption, PaymentOptionStatus> {

  private final PaymentOptionRepository paymentOptionRepository;

  public PaymentOptionStatusChecker(PaymentOptionRepository paymentOptionRepository) {
    super(InstallmentStatus.TO_SYNC, InstallmentStatus.PAID, InstallmentStatus.DRAFT,
      InstallmentStatus.UNPAID, InstallmentStatus.UNPAYABLE, InstallmentStatus.EXPIRED, InstallmentStatus.CANCELLED,
      InstallmentStatus.INVALID, InstallmentStatus.REPORTED);
    this.paymentOptionRepository = paymentOptionRepository;
  }

  @Override
  public PaymentOptionStatus calculateNewStatus(List<InstallmentStatus> installmentStatusList) {
    if (isToSync(installmentStatusList)) {
      return TO_SYNC;
    } else if (isPartiallyPaid(installmentStatusList)) {
      return PaymentOptionStatus.PARTIALLY_PAID;
    } else if (isDraft(installmentStatusList)) {
      return PaymentOptionStatus.DRAFT;
    } else if (isUnpaid(installmentStatusList)) {
      return PaymentOptionStatus.UNPAID;
    } else if (isPaid(installmentStatusList)) {
      return PaymentOptionStatus.PAID;
    } else if (isReported(installmentStatusList)) {
      return PaymentOptionStatus.REPORTED;
    } else if (isInvalid(installmentStatusList)) {
      return PaymentOptionStatus.INVALID;
    } else if (isUnpayable(installmentStatusList)) {
      return PaymentOptionStatus.UNPAYABLE;
    } else if (isCancelled(installmentStatusList)) {
      return PaymentOptionStatus.CANCELLED;
    } else if (isExpired(installmentStatusList)) {
      return PaymentOptionStatus.EXPIRED;
    } else {
      throw new InvalidValueException("[UNDETERMINED_PO_STATUS] Unable to determine status for PaymentOption having installmentStatuses: " + installmentStatusList);
    }
  }

  @Override
  protected List<InstallmentStatus> getChildStatuses(BasePaymentOption paymentOption) {
    return paymentOption.getInstallments().stream()
      .map(BaseInstallment::getStatus)
      .toList();
  }

  @Override
  protected void setStatus(BasePaymentOption paymentOption, PaymentOptionStatus newStatus) {
    paymentOption.setStatus(newStatus);
  }

  @Override
  protected void storeStatus(BasePaymentOption paymentOption, PaymentOptionStatus newStatus) {
    paymentOptionRepository.updateStatus(paymentOption.getPaymentOptionId(), newStatus);
  }

  @Override
  protected boolean isPartiallyPaid(List<InstallmentStatus> childrenStatusList) {
    return (childrenStatusList.contains(InstallmentStatus.PAID) || childrenStatusList.contains(InstallmentStatus.REPORTED)) &&
      (childrenStatusList.contains(InstallmentStatus.UNPAID) || childrenStatusList.contains(InstallmentStatus.EXPIRED));
  }

  @Override
  public boolean isPaid(List<InstallmentStatus> childrenStatusList) {
    return childrenStatusList.stream().anyMatch(InstallmentStatus.PAID::equals) &&
      childrenStatusList.stream().allMatch(
        status -> InstallmentStatus.PAID.equals(status)
          || InstallmentStatus.REPORTED.equals(status)
          || super.allowedCancelledStatuses.contains(status));
  }

  private boolean isInvalid(List<InstallmentStatus> childrenStatusList) {
    return childrenStatusList.contains(InstallmentStatus.INVALID) &&
      childrenStatusList.stream().allMatch(status -> InstallmentStatus.INVALID.equals(status) || InstallmentStatus.CANCELLED.equals(status));
  }
}
