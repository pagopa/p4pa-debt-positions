package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;

/**
 * Service class responsible to validate the new DebtPosition entity.
 * This class provides a method that validates mandatory fields or formality of field values about new debt position.
 */
public interface ValidateDebtPositionService {

    /**
     * Validates a new debt position values
     * @param debtPositionRequestDTO representing the new debt position to be validated
     * @param debtPositionTypeOrg representing the debt position type org
     * @throws InvalidValueException if a value does not comply with business rules
     */
    void validate(DebtPositionDTO debtPositionRequestDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg);

  /**
   * Validates a new installment values
   * @param installmentDTO representing the new installment to be validated
   * @param accessToken the access token
   * @param debtPositionTypeOrg representing the debt position type org
   * @param debtPositionOrigin representing the debt position origin
   * @throws InvalidValueException if a value does not comply with business rules
   */
    void validateInstallment(InstallmentDTO installmentDTO, String accessToken, DebtPositionTypeOrg debtPositionTypeOrg, DebtPositionOrigin debtPositionOrigin, Boolean flagPuPagoPaPayment);

    /**
     * Validates if exists difference debtor into same PaymentOption or if multiDebtor is disabled
     * @param debtPositionDTO representing the debt position to be validated
     */
    void validateDebtorConsistency(DebtPositionDTO debtPositionDTO);
}
