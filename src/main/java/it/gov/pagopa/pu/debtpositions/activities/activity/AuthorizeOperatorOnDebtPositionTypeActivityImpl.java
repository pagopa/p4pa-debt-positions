package it.gov.pagopa.pu.debtpositions.activities.activity;

import it.gov.pagopa.pu.debtpositions.activities.exception.OperatorNotAuthorizedException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgOperatorsRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Lazy
@Service
public class AuthorizeOperatorOnDebtPositionTypeActivityImpl implements AuthorizeOperatorOnDebtPositionTypeActivity {

    private final DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository;

    private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

    public AuthorizeOperatorOnDebtPositionTypeActivityImpl(DebtPositionTypeOrgOperatorsRepository debtPositionTypeOrgOperatorsRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
        this.debtPositionTypeOrgOperatorsRepository = debtPositionTypeOrgOperatorsRepository;
        this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    }

    public DebtPositionTypeOrg authorize(Long orgId, Long debtPositionTypeOrgId, String username){
        DebtPositionTypeOrgOperators debtPositionTypeOrgOperators =
                debtPositionTypeOrgOperatorsRepository.findDebtPositionTypeOrgOperatorsByOperatorExternalUserId(username);

        Optional<DebtPositionTypeOrg> debtPositionTypeOrg =
                debtPositionTypeOrgRepository.findDebtPositionTypeOrgByOrganizationIdAndDebtPositionTypeOrgId(orgId, debtPositionTypeOrgOperators.getDebtPositionTypeOrgId());

        return debtPositionTypeOrg
                .orElseThrow(() -> new OperatorNotAuthorizedException("The operator " + username + " is not authorized on the DebtPositionTypeOrg " + debtPositionTypeOrgId + " related to organization " + orgId));
    }
}
