package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.dto.LocalDateTimeIntervalFilter;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonEntityType;
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

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "debt-positions")
public interface DebtPositionRepository extends JpaRepository<DebtPosition, Long> {

  @RestResource(exported = false)
  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findEntityGraphByDebtPositionId(Long debtPositionId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE DebtPosition d SET d.status = :status WHERE d.debtPositionId = :debtPositionId")
  void updateStatus(Long debtPositionId, DebtPositionStatus status);

  @RestResource(exported = false)
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
  DebtPosition findEntityGraphByTransferId(Long transferId);

  @RestResource(exported = false)
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
  DebtPosition findEntityGraphByInstallmentId(Long installmentId);

  @Query("""
   SELECT d
   FROM DebtPosition d
      JOIN d.paymentOptions p
      JOIN p.installments i
   WHERE i.installmentId = :installmentId
   """)
  DebtPosition findByInstallmentId(Long installmentId);

  @RestResource(exported = false)
  @EntityGraph(value = "completeDebtPosition")
  DebtPosition findEntityGraphByIupdOrgAndOrganizationId(String iupdOrg, Long organizationId);

  @RestResource(exported = false)
  @Query("""
   SELECT DISTINCT d
   FROM DebtPosition d
   WHERE EXISTS (
      SELECT 1
      FROM PaymentOption p
         JOIN p.installments i
      WHERE p.debtPositionId = d.debtPositionId
        AND i.ingestionFlowFileId = :ingestionFlowFileId
        AND (:statusToExclude IS NULL OR i.status NOT IN :statusToExclude)
      )
   """)
  @EntityGraph(value = "completeDebtPosition")
  Page<DebtPosition> findEntityGraphByIngestionFlowFileIdAndStatusToExclude(Long ingestionFlowFileId,
                                                                            List<InstallmentStatus> statusToExclude,
                                                                            Pageable pageable);

  @Query("""
   SELECT d
   FROM DebtPosition d
      JOIN d.paymentOptions p
      JOIN p.installments i
   WHERE d.organizationId = :organizationId
      AND (:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins)
      AND i.nav = :nav
   """)
  List<DebtPosition> findByOrganizationIdAndInstallmentNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigins);

  @RestResource(exported = false)
  @Query("""
   SELECT d
     FROM DebtPosition d
    WHERE d.organizationId = :organizationId
      AND (:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins)
      AND EXISTS (
        SELECT 1
          FROM PaymentOption p
          JOIN p.installments i
         WHERE p.debtPositionId = d.debtPositionId
           AND i.iuv = :iuv
      )
  """)
  @EntityGraph(value = "completeDebtPosition")
  List<DebtPosition> findEntityGraphByOrganizationIdAndInstallmentIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigins);

  @RestResource(exported = false)
  @Query("""
   SELECT d
     FROM DebtPosition d
    WHERE d.organizationId = :organizationId
      AND (:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins)
      AND EXISTS (
        SELECT 1
          FROM PaymentOption p
          JOIN p.installments i
         WHERE p.debtPositionId = d.debtPositionId
           AND i.iuv IN :iuvs
           AND i.dueDate < current_date
      )
  """)
  @EntityGraph(value = "completeDebtPosition")
  List<DebtPosition> findEntityGraphByOrganizationIdAndExpiredIuvs(Long organizationId, List<String> iuvs, List<DebtPositionOrigin> debtPositionOrigins);

  @RestResource(exported = false)
  @Query("""
   SELECT d
     FROM DebtPosition d
    WHERE d.organizationId = :organizationId
      AND (:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins)
      AND EXISTS (
        SELECT 1
          FROM PaymentOption p
          JOIN p.installments i
         WHERE p.debtPositionId = d.debtPositionId
           AND i.receiptId = :receiptId
      )
  """)
  @EntityGraph(value = "completeDebtPosition")
  List<DebtPosition> findEntityGraphByOrganizationIdAndReceiptId(Long organizationId, Long receiptId, List<DebtPositionOrigin> debtPositionOrigins);

  @RestResource(exported = false)
  @Query("""
   SELECT d
     FROM DebtPosition d
    WHERE d.organizationId = :organizationId
      AND (:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins)
      AND EXISTS (
        SELECT 1
          FROM PaymentOption p
          JOIN p.installments i
         WHERE p.debtPositionId = d.debtPositionId
           AND i.iud = :iud
      )
  """)
  @EntityGraph(value = "completeDebtPosition")
  List<DebtPosition> findEntityGraphByOrganizationIdAndInstallmentIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigins);

  Page<DebtPosition> findByDebtPositionTypeOrgId(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId, Pageable pageable);

  @Query("""
   SELECT COUNT(dptoo)
   FROM DebtPosition d
        JOIN DebtPositionTypeOrg dpto on d.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
        JOIN DebtPositionTypeOrgOperators dptoo on d.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
     WHERE d.debtPositionId = :debtPositionId
     AND dptoo.operatorExternalUserId = :operatorExternalUserId
     AND d.organizationId = :organizationId
   """)
  long validateOperator(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionId") Long debtPositionId,
                        @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
                        @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);

  Optional<DebtPosition> findDebtPositionByIupdOrgAndOrganizationId(String iupd, Long organizationId);

  @RestResource(exported = false)
  @Query("""
   SELECT d
     FROM DebtPosition d
        JOIN DebtPositionTypeOrg dpto on d.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    WHERE d.organizationId IN :organizationIds
      AND (:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins)
      AND (:debtPositionTypeOrgCodesToExclude IS NULL OR dpto.code NOT IN :debtPositionTypeOrgCodesToExclude)
      AND EXISTS (
        SELECT 1
          FROM PaymentOption p
          JOIN p.installments i
         WHERE p.debtPositionId = d.debtPositionId
           AND i.debtorFiscalCodeHash = :debtorFiscalCodeHash
           AND i.debtorEntityType = :debtorEntityType
           AND (:status IS NULL OR i.status IN :status)
           AND (cast(:#{#dateTimeIntervalFilter.from} AS STRING) IS NULL OR i.updateDate >= :#{#dateTimeIntervalFilter.from})
           AND (cast(:#{#dateTimeIntervalFilter.to} AS STRING) IS NULL OR i.updateDate <= :#{#dateTimeIntervalFilter.to})
      )
  """)
  @EntityGraph(value = "completeDebtPosition")
  List<DebtPosition> findEntityGraphByDebtorFiscalCodeAndDebtorEntityType(
    @Param("debtorFiscalCodeHash") byte[] debtorFiscalCodeHash,
    @Param("debtorEntityType") PersonEntityType debtorEntityType,
    @Param("status") List<InstallmentStatus> status,
    @Param("debtPositionOrigins") List<DebtPositionOrigin> debtPositionOrigins,
    @Param("debtPositionTypeOrgCodesToExclude") List<String> debtPositionTypeOrgCodesToExclude,
    @Param("organizationIds") List<Long> organizationIds,
    @Param("dateTimeIntervalFilter") LocalDateTimeIntervalFilter dateTimeIntervalFilter
  );

  @RestResource(exported = false)
  @Query("""
    SELECT dp
    FROM DebtPosition dp
    WHERE
     dp.status IN (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).OPEN_DP_STATUSES})
     AND dp.organizationId IN :organizationIds
     AND EXISTS (
             SELECT 1
             FROM PaymentOption po
             JOIN po.installments i
             WHERE po.debtPositionId = dp.debtPositionId
             AND po.status IN (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PAYABLE_PO_STATUSES})
             AND i.status = 'UNPAID'
             AND i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)}
     )
    AND dp.debtPositionOrigin != 'SPONTANEOUS_MIXED'
  """)
  @EntityGraph(value = "completeDebtPosition")
  Page<DebtPosition> findPagedPrimaryDebtPositionByFilters(
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    @Parameter(name = "organizationIds", required = true, array = @ArraySchema(schema = @Schema(type = "integer", format = "int64")))@Param("organizationIds") List<Long> organizationIds,
    Pageable pageable
  );
}
