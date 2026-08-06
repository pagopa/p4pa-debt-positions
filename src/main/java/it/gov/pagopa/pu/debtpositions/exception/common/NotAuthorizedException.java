package it.gov.pagopa.pu.debtpositions.exception.common;

public class NotAuthorizedException extends BaseBusinessException {
  public NotAuthorizedException(String code, String message) {
    super(code, message);
  }
}
