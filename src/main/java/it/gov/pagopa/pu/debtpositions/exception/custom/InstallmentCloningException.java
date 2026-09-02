package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.exception.common.BaseBusinessException;

public class InstallmentCloningException extends BaseBusinessException {
  public InstallmentCloningException(String code, String message) {
    super(code, message);
  }
}
