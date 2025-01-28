package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidStatusTransitionException extends RuntimeException {

  public InvalidStatusTransitionException(String message) {
    super(message);
  }
}
