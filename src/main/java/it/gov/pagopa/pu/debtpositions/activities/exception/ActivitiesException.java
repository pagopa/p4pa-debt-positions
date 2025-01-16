package it.gov.pagopa.pu.debtpositions.activities.exception;

/**
 * A custom exception that represents errors related to activities and extends {@link RuntimeException}.
 *
 */
public class ActivitiesException extends RuntimeException {

    /**
     * Constructs a new {@code ActivitiesException} with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception.
     */
    public ActivitiesException(String message) {
        super(message);
    }
}
