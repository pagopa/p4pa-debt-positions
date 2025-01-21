package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidStatusException extends RuntimeException{

  public InvalidStatusException(String message)  {
    super(message);
  }
}
