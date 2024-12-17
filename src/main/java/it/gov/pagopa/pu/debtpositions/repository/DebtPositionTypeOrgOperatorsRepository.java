package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "debt_position_type_org_operators", path = "debtPositionTypeOrgOperators")
public interface DebtPositionTypeOrgOperatorsRepository extends JpaRepository<DebtPositionTypeOrgOperators,Long>{

}
