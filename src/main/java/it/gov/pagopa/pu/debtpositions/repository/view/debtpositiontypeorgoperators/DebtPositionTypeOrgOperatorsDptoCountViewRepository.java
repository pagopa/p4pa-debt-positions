package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontypeorgoperators;

import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorgoperators.DebtPositionTypeOrgOperatorsDptoCountView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Set;

@RepositoryRestResource(path = "debt-position-type-org-operators-count-view")
public interface DebtPositionTypeOrgOperatorsDptoCountViewRepository extends Repository<DebtPositionTypeOrgOperatorsDptoCountView, String> {
    @Query("""
          select new DebtPositionTypeOrgOperatorsDptoCountView(dptoo.operatorExternalUserId as operatorExternalUserId, count(dptoo.operatorExternalUserId) as debtPositionTypeOrgCount)
          from DebtPositionTypeOrgOperators dptoo
          join DebtPositionTypeOrg dpto on dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
          where dpto.organizationId = :organizationId
          and dptoo.operatorExternalUserId in :operatorExternalUserIds
          group by dptoo.operatorExternalUserId
          """)
    List<DebtPositionTypeOrgOperatorsDptoCountView> findByOrganizationIdAndOperatorExternalUserIds(Long organizationId, Set<String> operatorExternalUserIds);
}
