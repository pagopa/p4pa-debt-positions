package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebtPositionTypeOrgBalanceCostRepository extends JpaRepository<DebtPositionTypeOrgBalanceCost, Long> {
}
