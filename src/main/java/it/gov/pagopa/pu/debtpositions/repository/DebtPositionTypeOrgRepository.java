package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "debt-position-type-orgs")
public interface DebtPositionTypeOrgRepository extends JpaRepository<DebtPositionTypeOrg,Long> {

  Optional<DebtPositionTypeOrg> findByOrganizationIdAndDebtPositionTypeOrgId(Long organizationId, Long debtPositionTypeOrgId);
}
