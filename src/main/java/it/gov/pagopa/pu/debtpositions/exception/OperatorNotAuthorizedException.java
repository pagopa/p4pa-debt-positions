package it.gov.pagopa.pu.debtpositions.exception;


public class OperatorNotAuthorizedException extends RuntimeException {

    public OperatorNotAuthorizedException(String message) {
        super(message);
    }
}
