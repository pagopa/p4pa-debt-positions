package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorizeOperatorOnDebtPositionTypeServiceImpl implements AuthorizeOperatorOnDebtPositionTypeService {

    private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

    public AuthorizeOperatorOnDebtPositionTypeServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
      this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    }

  public DebtPositionTypeOrg authorize(Long debtPositionTypeOrgId, String operatorExternalUserId) {
    Optional<DebtPositionTypeOrg> debtPositionTypeOrg =
      debtPositionTypeOrgRepository.findByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId, operatorExternalUserId);

    return debtPositionTypeOrg
      .orElseThrow(() -> new OperatorNotAuthorizedException("The operator " + operatorExternalUserId + " is not authorized on the DebtPositionTypeOrg " + debtPositionTypeOrgId));

  }
}
