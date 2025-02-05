package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontype;

import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontype.DebtPositionTypeWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-types-with-count")
public interface DebtPositionTypeWithCountRepository extends Repository<DebtPositionTypeWithCount, Long> {

  @Query(value = "SELECT d "
    + "FROM DebtPositionTypeWithCount d "
    + "WHERE d.brokerId = :brokerId ")
  Page<DebtPositionTypeWithCount> findByBrokerId(@Param("brokerId") Long brokerId, Pageable pageable);

}
