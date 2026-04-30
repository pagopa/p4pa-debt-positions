package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidStatusTransitionException extends BaseBusinessException {

  public InvalidStatusTransitionException(String code, String message) {
    super(code, message);
  }
}
