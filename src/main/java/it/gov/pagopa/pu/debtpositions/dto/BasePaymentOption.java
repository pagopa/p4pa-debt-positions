package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;

import java.io.Serializable;
import java.util.Collection;

public interface BasePaymentOption extends Serializable {
  Long getPaymentOptionId();

  PaymentOptionType getPaymentOptionType();
  PaymentOptionStatus getStatus();
  void setStatus(PaymentOptionStatus status);

  void setTotalAmountCents(Long totalAmountCents);

  Collection<? extends BaseInstallment> getInstallments();
}
