package it.gov.pagopa.pu.debtpositions.enums;

import lombok.Getter;

@Getter
public enum PaymentOutcomeCode {

  PAYMENT_EXECUTED(0),
  PAYMENT_NOT_EXECUTED(1),
  PAYMENT_PARTIALLY_EXECUTED(2),
  TERMS_EXPIRED(3),
  PARTIAL_TERMS_EXPIRED(4);

  private final int code;

  PaymentOutcomeCode(int code) {
    this.code = code;
  }

}
