package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidDateTimeIntervalException extends BaseBusinessException {
  public InvalidDateTimeIntervalException(String code, String message) {
    super(code, message);
  }
}
