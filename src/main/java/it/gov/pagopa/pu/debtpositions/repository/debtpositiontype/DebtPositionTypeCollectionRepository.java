package it.gov.pagopa.pu.debtpositions.repository.debtpositiontype;

import it.gov.pagopa.pu.debtpositions.model.debtpositiontype.DebtPositionTypeWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-types-collection")
public interface DebtPositionTypeCollectionRepository extends JpaRepository<DebtPositionTypeWithCount,Long> {
  @Query(value = "SELECT t.debt_position_type_id, t.code, t.description, t.update_date, "
    + "( SELECT COUNT(org.organization_id) "
    + "FROM debt_position_type_org org "
    + "WHERE t.debt_position_type_id = org.debt_position_type_id ) AS active_organizations "
    + "FROM debt_position_type t "
    + "WHERE t.broker_id = :brokerId ",
    countQuery = "SELECT COUNT(*) FROM debt_position_type d WHERE d.broker_id = :brokerId",
    nativeQuery = true)
    Page<DebtPositionTypeWithCount> findByBrokerId(@Param("brokerId") Long brokerId, Pageable pageable);
}
