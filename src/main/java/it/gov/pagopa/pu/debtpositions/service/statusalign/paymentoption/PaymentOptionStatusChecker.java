package it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.service.statusalign.StatusRulesHandler;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus.TO_SYNC;

@Service
public class PaymentOptionStatusChecker extends StatusRulesHandler<InstallmentStatus>{

  public PaymentOptionStatusChecker() {
    super(InstallmentStatus.TO_SYNC, InstallmentStatus.PAID, InstallmentStatus.UNPAID,
      InstallmentStatus.EXPIRED, InstallmentStatus.CANCELLED, InstallmentStatus.REPORTED, InstallmentStatus.INVALID);
  }

  public PaymentOptionStatus determinePaymentOptionStatus(List<InstallmentStatus> installmentStatusList) {
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
      throw new InvalidStatusException("Unable to determine status for PaymentOption");
    }
  }
}
