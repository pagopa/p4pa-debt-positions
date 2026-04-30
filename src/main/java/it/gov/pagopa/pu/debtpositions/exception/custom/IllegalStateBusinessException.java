package it.gov.pagopa.pu.debtpositions.exception.custom;

public class IllegalStateBusinessException extends BaseBusinessException {
  public IllegalStateBusinessException(String code, String message) {
    this(code, message, null);
  }

  public IllegalStateBusinessException(String code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
