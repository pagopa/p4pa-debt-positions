package it.gov.pagopa.pu.debtpositions.exception.custom;

public class WorkflowErrorException extends BaseBusinessException {
  public WorkflowErrorException(String code, String message) {
    super(code, message);
  }
}
