package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontypeorg;

import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg.DebtPositionTypeOrgCountByOrganizationId;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-type-orgs-by-organization")
public interface DebtPositionTypeOrgByOrganizationRepository extends Repository<DebtPositionTypeOrgCountByOrganizationId, Long> {

  @Query(value = "SELECT distinct dpto "
    + "FROM DebtPositionTypeOrgCountByOrganizationId dpto "
    + "WHERE dpto.organizationId IN :organizationIds "
    + "AND dpto.flagActive = true ")
  List<DebtPositionTypeOrgCountByOrganizationId> countByOrganizationIds(List<Long> organizationIds);

}
