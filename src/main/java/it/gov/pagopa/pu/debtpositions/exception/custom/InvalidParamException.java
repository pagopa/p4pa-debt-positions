package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidParamException extends BaseBusinessException {
  public InvalidParamException(String code, String message) {
    super(code, message);
  }
}
