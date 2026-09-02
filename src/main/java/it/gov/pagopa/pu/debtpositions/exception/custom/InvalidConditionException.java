package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.exception.common.BaseBusinessException;

public class InvalidConditionException extends BaseBusinessException {
  public InvalidConditionException(String code, String message) {
    super(code, message);
  }
}
