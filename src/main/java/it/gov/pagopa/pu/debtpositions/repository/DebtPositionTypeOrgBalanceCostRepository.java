package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("DELETE FROM DebtPositionTypeOrgBalanceCost dptobc WHERE dptobc.id.debtPositionTypeOrgId = :debtPositionTypeOrgId")
  void deleteByDebtPositionTypeOrgId(@Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId);

  @Query("SELECT dptobc FROM DebtPositionTypeOrgBalanceCost dptobc " +
    "WHERE dptobc.id.debtPositionTypeOrgId = :debtPositionTypeOrgId " +
    "AND dptobc.id.operatingYear = :operatingYear")
  List<DebtPositionTypeOrgBalanceCost> getByDebtPositionTypeOrgIdAndOperatingYear(
    long debtPositionTypeOrgId,
    String operatingYear
  );

  @Query("SELECT dptobc FROM DebtPositionTypeOrgBalanceCost dptobc " +
    "WHERE dptobc.id.debtPositionTypeOrgId = :debtPositionTypeOrgId " +
    "AND dptobc.id.operatingYear IN :operatingYears")
  List<DebtPositionTypeOrgBalanceCost> getByDebtPositionTypeOrgIdAndOperatingYears(
    long debtPositionTypeOrgId,
    List<String> operatingYears
  );

  @Query("SELECT dptobc FROM DebtPositionTypeOrgBalanceCost dptobc " +
    "WHERE dptobc.id.debtPositionTypeOrgId = :debtPositionTypeOrgId " +
    "AND dptobc.id.operatingYear = :operatingYear " +
    "AND dptobc.id.type = :type")
  Optional<DebtPositionTypeOrgBalanceCost> getByDebtPositionTypeOrgIdAndOperatingYearAndType(
    long debtPositionTypeOrgId,
    String operatingYear,
    DebtPositionTypeOrgBalanceCostType type
  );
}
