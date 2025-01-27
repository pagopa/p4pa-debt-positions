package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidInstallmentStatusException extends RuntimeException {

  public InvalidInstallmentStatusException(String message) {
    super(message);
  }
}
