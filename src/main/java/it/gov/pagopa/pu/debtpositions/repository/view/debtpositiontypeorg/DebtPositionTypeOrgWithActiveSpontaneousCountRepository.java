package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontypeorg;

import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg.DebtPositionTypeOrgWithActiveSpontaneousCount;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "debt-position-type-org-with-active-spontaneous-count")
public interface DebtPositionTypeOrgWithActiveSpontaneousCountRepository extends Repository<DebtPositionTypeOrgWithActiveSpontaneousCount, Long> {

  @Query(value = "SELECT distinct dpto "
    + "FROM DebtPositionTypeOrgWithActiveSpontaneousCount dpto "
    + "WHERE dpto.organizationId IN :organizationIds ")
  List<DebtPositionTypeOrgWithActiveSpontaneousCount> countByOrganizationIds(List<Long> organizationIds);
}
