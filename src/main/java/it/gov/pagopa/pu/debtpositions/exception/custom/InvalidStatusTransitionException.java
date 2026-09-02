package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.exception.common.BaseBusinessException;

public class InvalidStatusTransitionException extends BaseBusinessException {

  public InvalidStatusTransitionException(String code, String message) {
    super(code, message);
  }
}
