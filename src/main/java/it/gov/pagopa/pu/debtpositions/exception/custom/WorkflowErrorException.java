package it.gov.pagopa.pu.debtpositions.exception.custom;

public class WorkflowErrorException extends RuntimeException {
  public WorkflowErrorException(String message) {
    super(message);
  }
}
