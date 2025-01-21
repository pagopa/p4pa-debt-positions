package it.gov.pagopa.pu.debtpositions.service;


import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.exception.OperatorNotAuthorizedException;

/**
 * Service class responsible for verifying authorization on the DebtPositionType entity.
 * This class provides methods to verify whether an operator has authorization to manage installment types
 * associated with a particular organization.
 */
public interface AuthorizeOperatorOnDebtPositionTypeService {


    /**
     * Verifies if the specified operator has authorization to manage a specific debt position type for an organization.
     * <p>
     * This method retrieves an authorized {@link DebtPositionTypeOrg} associated with the specified organization
     * and checks if the operator is authorized to manage it based on the provided {@code orgId},
     * {@code debtPositionTypeOrgId}, and {@code username}.
     * </p>
     *
     * @param orgId the identifier of the organization
     * @param debtPositionTypeOrgId the identifier of the specific debt position type to verify for authorization
     * @param username the username of the operator
     * @return the {@link DebtPositionTypeOrg} representing the authorized debt position type
     * @throws OperatorNotAuthorizedException if the operator is not authorized to manage the specified installment type
     */

    DebtPositionTypeOrg authorize(Long orgId, Long debtPositionTypeOrgId, String username);
}
