package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-type-org-balance-costs")
public interface DebtPositionTypeOrgBalanceCostRepository extends JpaRepository<DebtPositionTypeOrgBalanceCost, DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId> {
  @Query("select dptobc from DebtPosition dp " +
    "join dp.paymentOptions po " +
    "join po.installments i " +
    "join DebtPositionTypeOrgBalanceCost dptobc on dptobc.id.debtPositionTypeOrgId = dp.debtPositionTypeOrgId " +
    "where i.installmentId = :installmentId " +
    "and dptobc.id.type = :type " +
    "and dptobc.id.operatingYear = :operatingYear")
  DebtPositionTypeOrgBalanceCost getByInstallmentIdAndTypeAndOperatingYear(
    long installmentId,
    DebtPositionTypeOrgBalanceCostType type,
    String operatingYear
  );
}
