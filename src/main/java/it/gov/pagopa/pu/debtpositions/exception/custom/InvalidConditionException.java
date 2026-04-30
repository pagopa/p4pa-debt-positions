package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidConditionException extends BaseBusinessException {
  public InvalidConditionException(String code, String message) {
    super(code, message);
  }
}
