package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    SELECT COUNT(i)
    FROM DebtPosition dp
    JOIN dp.paymentOptions po
    JOIN po.installments i
    WHERE dp.organizationId = :orgId
      AND i.status <> 'CANCELLED'
      AND ((i.iud = :iud) OR (:iuv IS NOT NULL AND i.iuv = :iuv) OR (:nav IS NOT NULL AND i.nav = :nav))
    """)
  long countExistingInstallments(Long orgId, String iud, String iuv, String nav);

  @Query(value = "SELECT i from InstallmentNoPII i " +
    "JOIN Transfer t ON i.installmentId = t.installmentId " +
    "JOIN PaymentOption p ON i.paymentOptionId = p.paymentOptionId " +
    "JOIN DebtPosition d ON p.debtPositionId = d.debtPositionId " +
    "JOIN DebtPositionTypeOrgOperators dptoo ON d.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId " +
    "WHERE d.organizationId = :organizationId AND " +
    "i.iuv = :iuv AND " +
    "i.iur = :iur AND " +
    "t.transferIndex = :transferIndex AND " +
    "dptoo.operatorExternalUserId = :operatorExternalUserId")
  Optional<InstallmentNoPII> findAuthorizedByTransferSemanticKey(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("iuv") String iuv,
    @Parameter(required = true) @Param("iur") String iur,
    @Parameter(required = true) @Param("transferIndex") int transferIndex,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);

  @RestResource(exported = false)
  @Query(" select i" +
    "  from InstallmentNoPII i" +
    "  join PaymentOption po" +
    "    on i.paymentOptionId = po.paymentOptionId" +
    "  join DebtPosition dp" +
    "    on po.debtPositionId = dp.debtPositionId" +
    " where dp.organizationId = :organizationId" +
    "   and (:debtPositionOrigins is null or dp.debtPositionOrigin in (:debtPositionOrigins))" +
    "   and i.nav = :nav")
  List<InstallmentNoPII> getByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigins);

  // region API CRUD
  List<InstallmentNoPII> findByReceiptId(long receiptId);
  //endregion

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
  List<InstallmentNoPII> findByDebtPositionIdAndStatuses(Long debtPositionId, List<InstallmentStatus> installmentStatuses);

}
