package it.gov.pagopa.pu.debtpositions.exception.custom;

public class ExportTooManyRecordsException extends BaseBusinessException {
  public ExportTooManyRecordsException(String code, String message) {
    super(code, message);
  }
}
