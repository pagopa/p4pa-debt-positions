package it.gov.pagopa.pu.debtpositions.exception.custom;

import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;

import java.util.List;

public class InvalidValueException extends BaseBusinessException {

  public InvalidValueException(String code, String message) {
    this(code, message, null);
  }

  public InvalidValueException(String code, String message, List<ErrorFieldDTO> fieldErrors) {
    super(code, message, fieldErrors, null);
  }
}
