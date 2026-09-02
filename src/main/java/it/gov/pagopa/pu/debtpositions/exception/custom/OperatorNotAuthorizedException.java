package it.gov.pagopa.pu.debtpositions.exception.custom;


import it.gov.pagopa.pu.debtpositions.exception.common.ForbiddenException;

@SuppressWarnings("java:S110") // Suppress "Inheritance tree of classes should not be too deep": allowed for exception hierarchy
public class OperatorNotAuthorizedException extends ForbiddenException {

    public OperatorNotAuthorizedException(String code, String message) {
        super(code, message);
    }
}
