package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontypeorg;

import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg.DebtPositionTypeOrgWithActiveOrganizationsCount;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-type-orgs-with-active-organizations-count")
public interface DebtPositionTypeOrgWithActiveOrganizationsCountRepository extends Repository<DebtPositionTypeOrgWithActiveOrganizationsCount, Long> {

  @Query(value = "SELECT distinct dpto "
    + "FROM DebtPositionTypeOrgWithActiveOrganizationsCount dpto "
    + "WHERE dpto.organizationId IN :organizationIds ")
  List<DebtPositionTypeOrgWithActiveOrganizationsCount> countByOrganizationIds(List<Long> organizationIds);

}
