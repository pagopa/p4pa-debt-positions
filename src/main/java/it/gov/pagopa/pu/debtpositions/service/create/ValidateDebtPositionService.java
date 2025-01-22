package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

/**
 * Service class responsible to validate the new DebtPosition entity.
 * This class provides a method that validates mandatory fields or formality of field values about new debt position.
 */
public interface ValidateDebtPositionService {

    /**
     * Validates a new debt position values
     * @param debtPositionRequestDTO representing the new debt position to be validated
     * @throws InvalidValueException if a value does not comply with business rules
     */
    void validate(DebtPositionDTO debtPositionRequestDTO, String accessToken);
}
