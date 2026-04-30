package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RepositoryRestResource(path = "installments")
public interface InstallmentNoPIIRepository extends JpaRepository<InstallmentNoPII, Long> {

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.status = :status, i.syncStatus = :syncStatus WHERE i.installmentId = :installmentId")
  @ApiResponse(responseCode = "200", description = "Status updated successfully")
  void updateStatus(Long installmentId, InstallmentStatus status, InstallmentSyncStatus syncStatus);

  @Transactional
  @RestResource(exported = false)
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.dueDate = :dueDate WHERE i.installmentId = :installmentId")
  void updateDueDate(Long installmentId, LocalDate dueDate);

  @Transactional
  @RestResource(exported = false)
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.iun = :iun " +
    "WHERE i.paymentOptionId IN (" +
    "  SELECT po.paymentOptionId FROM PaymentOption po WHERE po.debtPositionId = :debtPositionId" +
    ")")
  void updateIunByDebtPositionId(Long debtPositionId, String iun);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.status = :status, i.iuf = :iuf WHERE i.installmentId = :installmentId")
  @ApiResponse(responseCode = "200", description = "Status updated successfully")
  void updateStatusAndIuf(Long installmentId, InstallmentStatus status, String iuf);

  @Query("""
    SELECT CASE WHEN EXISTS (
      SELECT 1
      FROM DebtPosition dp
      JOIN dp.paymentOptions po
      JOIN po.installments i
      WHERE dp.organizationId = :orgId
        AND (:debtPositionOrigins IS NULL OR dp.debtPositionOrigin IN :debtPositionOrigins)
        AND i.status <> 'CANCELLED'
        AND ((i.iud = :iud) OR (:iuv IS NOT NULL AND i.iuv = :iuv) OR (:nav IS NOT NULL AND i.nav = :nav))
    ) THEN true ELSE false END
    """)
  boolean isInstallmentExists(Long orgId, String iud, String iuv, String nav, List<DebtPositionOrigin> debtPositionOrigins);

  @Query(value = "SELECT i from InstallmentNoPII i " +
    "JOIN Transfer t ON i.installmentId = t.installmentId " +
    "JOIN PaymentOption p ON i.paymentOptionId = p.paymentOptionId " +
    "JOIN DebtPosition d ON p.debtPositionId = d.debtPositionId " +
    "JOIN DebtPositionTypeOrgOperators dptoo ON d.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId " +
    "WHERE d.organizationId = :organizationId AND " +
    "(:debtPositionOrigins IS NULL OR d.debtPositionOrigin IN :debtPositionOrigins) AND " +
    "i.iuv = :iuv AND " +
    "i.iur = :iur AND " +
    "t.transferIndex = :transferIndex AND " +
    "dptoo.operatorExternalUserId = :operatorExternalUserId")
  Optional<InstallmentNoPII> findAuthorizedByTransferSemanticKey(
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("iuv") String iuv,
    @Parameter(required = true) @Param("iur") String iur,
    @Parameter(required = true) @Param("transferIndex") int transferIndex,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @RequestParam(required = false) @Param("debtPositionOrigins") List<DebtPositionOrigin> debtPositionOrigins);

  @RestResource(exported = false)
  @Query(" select i" +
    "  from InstallmentNoPII i" +
    "  join PaymentOption po" +
    "    on i.paymentOptionId = po.paymentOptionId" +
    "  join DebtPosition dp" +
    "    on po.debtPositionId = dp.debtPositionId" +
    " where dp.organizationId = :organizationId" +
    "   and (dp.debtPositionOrigin in (:debtPositionOrigins))" +
    "   and i.nav = :nav")
  List<InstallmentNoPII> getByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigins);

  @Query(" select i" +
    "  from InstallmentNoPII i" +
    "  join PaymentOption po" +
    "    on i.paymentOptionId = po.paymentOptionId" +
    "  join DebtPosition dp" +
    "    on po.debtPositionId = dp.debtPositionId" +
    " where dp.organizationId = :organizationId" +
    "   and (:installmentStatuses is null or i.status in (:installmentStatuses))" +
    "   and i.iud = :iud")
  List<InstallmentNoPII> getByOrganizationIdAndIudAndStatus(Long organizationId, String iud, List<InstallmentStatus> installmentStatuses);

  @RestResource(exported = false)
  @Query(" select i" +
    "  from InstallmentNoPII i" +
    "   where i.status in (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PAID_STATUSES})" +
    "   and i.iun = :iun")
  List<InstallmentNoPII> findPaidByIun(@Param("iun") String iun);

  @Query("  select i" +
    "  from InstallmentNoPII i" +
    "  join PaymentOption po" +
    "  on i.paymentOptionId = po.paymentOptionId" +
    "  where po.debtPositionId = :debtPositionId " +
    "  and (:installmentStatuses is null or i.status in (:installmentStatuses))")
  List<InstallmentNoPII> findByDebtPositionIdAndStatuses(
    @Parameter(required = true) @Param("debtPositionId") Long debtPositionId,
    @Parameter(required = true) @Param("installmentStatuses") List<InstallmentStatus> installmentStatuses);

  @Query(" select i" +
    "  from InstallmentNoPII i" +
    "  join PaymentOption po" +
    "    on i.paymentOptionId = po.paymentOptionId" +
    "  join DebtPosition dp" +
    "    on po.debtPositionId = dp.debtPositionId" +
    " where dp.organizationId = :organizationId" +
    "   and i.iud in :iuds")
  List<InstallmentNoPII> findByOrganizationIdAndIuds(Long organizationId, Set<String> iuds);

  @Query(" select i" +
    "  from InstallmentNoPII i" +
    "  join PaymentOption po" +
    "    on i.paymentOptionId = po.paymentOptionId" +
    "  join DebtPosition dp" +
    "    on po.debtPositionId = dp.debtPositionId" +
    " where i.receiptId = :receiptId" +
    " and dp.organizationId = :organizationId" +
    " and (:debtPositionOrigins is null or dp.debtPositionOrigin in (:debtPositionOrigins))")
  List<InstallmentNoPII> getByOrganizationIdAndReceiptId(
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("receiptId") Long receiptId,
    @RequestParam(required = false) @Param("debtPositionOrigins") List<DebtPositionOrigin> debtPositionOrigins);

  @RestResource(exported = false)
  @Query("""
    select i
    from InstallmentNoPII i
    join PaymentOption po on i.paymentOptionId = po.paymentOptionId
    join DebtPosition dp on po.debtPositionId = dp.debtPositionId
    where (:statuses IS NULL OR i.status IN (:statuses))
    AND (i.iuv = :iuvOrNav OR i.nav = :iuvOrNav)
    AND ((:debtorFiscalCode is null) OR (i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)} ))
    AND ((:organizationId is null) OR (dp.organizationId = :organizationId))
    AND dp.debtPositionOrigin in (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PRIMARY_ORG_DEBT_POSITION_ORIGINS_NO_MIXED})
    """)
  List<InstallmentNoPII> findByIuvOrNav(
    @Parameter(required = true) @Param("iuvOrNav") String iuvOrNav,
    String debtorFiscalCode,
    Long organizationId,
    List<InstallmentStatus> statuses);

  @Query("""
    SELECT i
    FROM InstallmentNoPII i
    JOIN PaymentOption po on i.paymentOptionId = :paymentOptionId
    JOIN DebtPosition dp on po.debtPositionId = :debtPositionId
    WHERE i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)}
    AND i.status in (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).UNPAID_OR_PAID_INSTALLMENT_STATUSES})
    AND dp.organizationId = :organizationId
    """)
  List<InstallmentNoPII> findDebtorUnpaidOrPaidByDebtPositionIdAndPaymentOptionId(
    @Parameter(required = true) @Param("debtPositionId") Long debtPositionId,
    @Parameter(required = true) @Param("paymentOptionId") Long paymentOptionId,
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    @Parameter(required = true) @Param("organizationId") Long organizationId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.balance = :balance WHERE i.installmentId = :installmentId")
  void updateBalance(Long installmentId, String balance);

  @Query("""
    select distinct i
    from InstallmentNoPII i
    join Transfer t on t.installmentId = i.installmentId
    where t.orgFiscalCode = :orgFiscalCode
    and i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)}
    and t.flagOwner = true
""")
  Page<InstallmentNoPII> findDistinctByOrgFiscalCodeAndDebtorFiscalCode(
    @Parameter(required = true) @Param("orgFiscalCode") String orgFiscalCode,
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    Pageable pageable
  );

  @RestResource(exported = false)
  @Query("""
    SELECT DISTINCT i.installmentId
    FROM InstallmentNoPII i
    WHERE i.installmentId IN :installmentIds
   """)
  Set<Long> findExistingInstallmentIds(List<Long> installmentIds);
}
