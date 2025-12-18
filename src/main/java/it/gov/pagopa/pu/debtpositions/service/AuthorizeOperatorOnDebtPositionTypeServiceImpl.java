package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.exception.custom.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.SecurityUtils.SYSTEM_USERID_PREFIX;

@Service
public class AuthorizeOperatorOnDebtPositionTypeServiceImpl implements AuthorizeOperatorOnDebtPositionTypeService {

  private static final String ORG_USER_ID_PREFIX = "WS_USER-";

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository;

  public AuthorizeOperatorOnDebtPositionTypeServiceImpl(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionTypeOrgOperatorsRepository = debtPositionTypeOrgOperatorsRepository;
  }

  public DebtPositionTypeOrg authorize(String orgIpaCode, Long debtPositionTypeOrgId, String operatorExternalUserId) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
      .orElseThrow(() -> new NotFoundException("[P4PA_MISSING_DEBT_POS_TYPE_ORG] The DebtPositionTypeOrg with id " + debtPositionTypeOrgId + " was not found"));

    if (!isOperatorAuthorized(orgIpaCode, debtPositionTypeOrgId, operatorExternalUserId)) {
      throw new OperatorNotAuthorizedException("[P4PA_DEBT_POS_TYPE_ORG_UNAUTHORIZED] The operator " + operatorExternalUserId + " is not authorized on the DebtPositionTypeOrg " + debtPositionTypeOrgId);
    }

    return debtPositionTypeOrg;
  }

  private boolean isOperatorAuthorized(String orgIpaCode, Long debtPositionTypeOrgId, String operatorExternalUserId) {
    if (operatorExternalUserId.startsWith(SYSTEM_USERID_PREFIX) || operatorExternalUserId.startsWith(ORG_USER_ID_PREFIX + orgIpaCode)) {
      return true;
    }
    Optional<DebtPositionTypeOrgOperators> debtPositionTypeOrgOperators =
      debtPositionTypeOrgOperatorsRepository.findByDebtPositionTypeOrgIdAndOperatorExternalUserId(debtPositionTypeOrgId, operatorExternalUserId);
    return debtPositionTypeOrgOperators.isPresent();
  }
}
