package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "debt_position_type", path = "debt-position-type")
public interface DebtPositionTypeRepository extends JpaRepository<DebtPositionType,Long> {

}
