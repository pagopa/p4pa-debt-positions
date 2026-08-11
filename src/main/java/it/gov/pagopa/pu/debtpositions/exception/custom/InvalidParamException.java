package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.exception.common.BaseBusinessException;

public class InvalidParamException extends BaseBusinessException {
  public InvalidParamException(String code, String message) {
    super(code, message);
  }
}
