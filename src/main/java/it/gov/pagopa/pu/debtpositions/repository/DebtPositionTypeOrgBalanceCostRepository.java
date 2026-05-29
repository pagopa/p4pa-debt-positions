package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DebtPositionTypeOrgBalanceCostRepository extends JpaRepository<DebtPositionTypeOrgBalanceCost, Long> {
  Optional<DebtPositionTypeOrgBalanceCost> findByDebtPositionTypeOrgIdAndTypeAndOperatingYear(Long debtPositionTypeOrgId, DebtPositionTypeOrgBalanceCostType type, String operatingYear);
}
