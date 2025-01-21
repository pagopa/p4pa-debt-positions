package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorizeOperatorOnDebtPositionTypeServiceImpl implements AuthorizeOperatorOnDebtPositionTypeService {

    private final DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository;

    private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

    public AuthorizeOperatorOnDebtPositionTypeServiceImpl(DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
        this.debtPositionTypeOrgOperatorsRepository = debtPositionTypeOrgOperatorsRepository;
        this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    }

    public DebtPositionTypeOrg authorize(Long orgId, Long debtPositionTypeOrgId, String operatorExternalUserId){
        DebtPositionTypeOrgOperators debtPositionTypeOrgOperators =
                debtPositionTypeOrgOperatorsRepository.findByOperatorExternalUserId(operatorExternalUserId);

        Optional<DebtPositionTypeOrg> debtPositionTypeOrg =
                debtPositionTypeOrgRepository.findByOrganizationIdAndDebtPositionTypeOrgId(orgId, debtPositionTypeOrgOperators.getDebtPositionTypeOrgId());

        return debtPositionTypeOrg
                .orElseThrow(() -> new OperatorNotAuthorizedException("The operator " + operatorExternalUserId + " is not authorized on the DebtPositionTypeOrg " + debtPositionTypeOrgId + " related to organization " + orgId));
    }
}
