package it.gov.pagopa.pu.debtpositions.exception.custom;


public class OperatorNotAuthorizedException extends RuntimeException {

    public OperatorNotAuthorizedException(String message) {
        super(message);
    }
}
