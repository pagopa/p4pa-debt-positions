package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.exception.common.BaseBusinessException;

public class ExportTooManyRecordsException extends BaseBusinessException {
  public ExportTooManyRecordsException(String code, String message) {
    super(code, message);
  }
}
