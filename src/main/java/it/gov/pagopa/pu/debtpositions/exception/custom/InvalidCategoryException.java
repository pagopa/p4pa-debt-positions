package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidCategoryException extends RuntimeException {
  public InvalidCategoryException(String message) {
    super(message);
  }
}
