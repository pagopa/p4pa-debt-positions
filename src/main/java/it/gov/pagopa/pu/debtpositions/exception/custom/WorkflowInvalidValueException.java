package it.gov.pagopa.pu.debtpositions.exception.custom;

public class WorkflowInvalidValueException extends RuntimeException {
  public WorkflowInvalidValueException(String message) {
    super(message);
  }
}
