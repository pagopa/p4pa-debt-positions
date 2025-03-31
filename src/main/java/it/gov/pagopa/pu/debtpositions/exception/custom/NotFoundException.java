package it.gov.pagopa.pu.debtpositions.exception.custom;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
            super(message);
        }
}
