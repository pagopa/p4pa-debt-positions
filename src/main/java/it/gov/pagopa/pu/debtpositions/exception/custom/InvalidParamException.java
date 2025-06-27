package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidParamException extends RuntimeException {
  public InvalidParamException(String message) {
    super(message);
  }
}
