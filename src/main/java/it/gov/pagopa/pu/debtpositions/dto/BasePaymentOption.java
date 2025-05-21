package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;

import java.io.Serializable;
import java.util.Collection;

public interface BasePaymentOption extends Serializable {
  Long getPaymentOptionId();

  PaymentOptionStatus getStatus();
  void setStatus(PaymentOptionStatus status);

  void setTotalAmountCents(Long totalAmountCents);

  Collection<? extends BaseInstallment> getInstallments();
}
