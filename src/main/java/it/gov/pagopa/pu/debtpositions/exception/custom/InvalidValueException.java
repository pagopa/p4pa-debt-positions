package it.gov.pagopa.pu.debtpositions.exception.custom;

public class InvalidValueException extends BaseBusinessException {

    public InvalidValueException(String code, String message) {
            super(code, message);
        }
}
