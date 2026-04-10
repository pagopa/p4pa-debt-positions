package it.gov.pagopa.pu.debtpositions.exception.custom;

public class ConflictErrorException extends BaseBusinessException {

    public ConflictErrorException(String code, String message) {
            super(code, message);
        }
}
