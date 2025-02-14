package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-types")
public interface DebtPositionTypeRepository extends JpaRepository<DebtPositionType,Long> {

  DebtPositionType findByDebtPositionTypeId(Long debtPositionTypeId);
}
