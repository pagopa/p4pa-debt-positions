package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "debt-positions")
public interface DebtPositionRepository extends JpaRepository<DebtPosition, Long> {

  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findOneWithAllDataByDebtPositionId(Long debtPositionId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE DebtPosition d SET d.status = :status WHERE d.debtPositionId = :debtPositionId")
  void updateStatus(Long debtPositionId, DebtPositionStatus status);

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
  DebtPosition findByTransferId(Long transferId);

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
  DebtPosition findByInstallmentId(Long installmentId);

  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findByIupdOrgAndOrganizationId(String iupdOrg, Long organizationId);

  @Query("""
   SELECT DISTINCT d
   FROM DebtPosition d
   WHERE EXISTS (
      SELECT 1
      FROM PaymentOption p
         JOIN p.installments i
      WHERE p.debtPositionId = d.debtPositionId AND i.ingestionFlowFileId = :ingestionFlowFileId
      )
   """)
  @EntityGraph(value = "completeDebtPosition")
  Page<DebtPosition> findByIngestionFlowFileId(Long ingestionFlowFileId,
                                               Pageable pageable);

  @Query("""
   SELECT d
   FROM DebtPosition d
      JOIN d.paymentOptions p
      JOIN p.installments i
   WHERE d.organizationId = :organizationId AND i.nav = :nav
   """)
  DebtPosition findByOrganizationIdAndInstallmentNav(Long organizationId, String nav);

  Page<DebtPosition> findByDebtPositionTypeOrgId(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId, Pageable pageable);
}
