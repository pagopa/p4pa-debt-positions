package it.gov.pagopa.pu.debtpositions.exception.custom;

public class ConflictErrorException extends RuntimeException {

    public ConflictErrorException(String message) {
            super(message);
        }
}
