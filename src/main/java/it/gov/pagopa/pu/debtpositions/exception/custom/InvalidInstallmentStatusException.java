package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.exception.common.BaseBusinessException;

public class InvalidInstallmentStatusException extends BaseBusinessException {
  public InvalidInstallmentStatusException(String code, String message) {
    super(code, message);
  }
}
