package it.gov.pagopa.pu.debtpositions.exception.custom;

public class DebtPositionNotFoundException extends RuntimeException {

    public DebtPositionNotFoundException(String message) {
        super(message);
    }
}
