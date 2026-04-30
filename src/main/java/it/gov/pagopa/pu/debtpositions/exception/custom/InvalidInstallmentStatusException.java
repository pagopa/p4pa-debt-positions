package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidInstallmentStatusException extends BaseBusinessException{
  public InvalidInstallmentStatusException(String code, String message) {
    super(code, message);
  }
}
