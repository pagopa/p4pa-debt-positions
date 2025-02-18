package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.OffsetDateTime;
import java.util.Optional;

@RepositoryRestResource(path = "debt-positions")
public interface DebtPositionRepository extends JpaRepository<DebtPosition, Long> {

  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findOneWithAllDataByDebtPositionId(Long debtPositionId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE DebtPosition d SET d.status = :status WHERE d.debtPositionId = :debtPositionId")
  void updateStatus(@Param("debtPositionId") Long debtPositionId, @Param("status") DebtPositionStatus status);

  @Query("""
   SELECT d
   FROM DebtPosition d
   WHERE EXISTS (
      SELECT 1
      FROM PaymentOption p
         JOIN p.installments i
         JOIN i.transfers t
      WHERE p.debtPositionId = d.debtPositionId AND t.transferId = :transferId
      )
   """)
  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findByTransferId(@Param("transferId") Long transferId);

  @EntityGraph(value = "completeDebtPosition")
  Optional<DebtPosition> findByIupdOrg(String iupdOrg);

  @Query("""
   SELECT d
   FROM DebtPosition d
   WHERE EXISTS (
      SELECT 1
      FROM PaymentOption p
         JOIN p.installments i
      WHERE p.debtPositionId = d.debtPositionId AND i.installmentId = :installmentId
      )
   """)
  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findByInstallmentId(@Param("installmentId") Long installmentId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE DebtPosition d " +
    "SET d.description = :description, d.validityDate = :validityDate " +
    "WHERE d.debtPositionId = :debtPositionId")
  void update(Long debtPositionId, String description, OffsetDateTime validityDate);
}
