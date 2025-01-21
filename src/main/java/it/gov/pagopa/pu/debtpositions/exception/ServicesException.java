package it.gov.pagopa.pu.debtpositions.exception;

/**
 * A custom exception that represents errors related to services and extends {@link RuntimeException}.
 *
 */
public class ServicesException extends RuntimeException {

    /**
     * Constructs a new {@code ServicesException} with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception.
     */
    public ServicesException(String message) {
        super(message);
    }
}
