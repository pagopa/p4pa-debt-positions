package it.gov.pagopa.pu.debtpositions.exception.custom;

public class NotFoundException extends BaseBusinessException {

    public NotFoundException(String code, String message) {
            super(code, message);
        }
}
