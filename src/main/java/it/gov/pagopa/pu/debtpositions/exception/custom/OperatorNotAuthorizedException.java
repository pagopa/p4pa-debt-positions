package it.gov.pagopa.pu.debtpositions.exception.custom;


public class OperatorNotAuthorizedException extends BaseBusinessException {

    public OperatorNotAuthorizedException(String code, String message) {
        super(code, message);
    }
}
