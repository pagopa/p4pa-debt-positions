package it.gov.pagopa.pu.debtpositions.exception.custom;

public class WorkflowNotAuthorizedException extends RuntimeException {
  public WorkflowNotAuthorizedException(String message) {
    super(message);
  }
}
