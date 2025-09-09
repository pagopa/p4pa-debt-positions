package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RepositoryRestResource(path = "debt-position-type-orgs")
public interface DebtPositionTypeOrgRepository extends JpaRepository<DebtPositionTypeOrg, Long> {

  Optional<DebtPositionTypeOrg> findByOrganizationIdAndDebtPositionTypeOrgId(Long organizationId, Long debtPositionTypeOrgId);

  Optional<DebtPositionTypeOrg> findByOrganizationIdAndCode(Long organizationId, String code);

  @Query(value = "SELECT dpto "
    + "FROM DebtPositionTypeOrg dpto "
    + "JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId "
    + "WHERE dpto.organizationId = :organizationId "
    + "AND dptoo.operatorExternalUserId = :operatorExternalUserId "
    + "AND (:flagActive IS NULL OR dpto.flagActive= :flagActive) ")
  List<DebtPositionTypeOrg> findDebtPositionTypeOrgs(@Parameter(required = true) @Param("organizationId") Long organizationId,
                                                     @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
                                                     @Param("flagActive")  Boolean flagActive);

  Page<DebtPositionTypeOrg> findByDebtPositionTypeId(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionTypeId") Long debtPositionTypeId,
                                                     Pageable pageable);

  @Query("select dpto from InstallmentNoPII i " +
    "join PaymentOption po on i.paymentOptionId = po.paymentOptionId " +
    "join DebtPosition dp on po.debtPositionId = dp.debtPositionId " +
    "join DebtPositionTypeOrg dpto on dpto.debtPositionTypeOrgId = dp.debtPositionTypeOrgId " +
    "where i.installmentId = :installmentId")
  DebtPositionTypeOrg getDebtPositionTypeOrgByInstallmentId(long installmentId);

  @RestResource(exported = false)
  @Override
  void deleteById(Long debtPositionTypeOrgId);

  @Query("""
    select dpto
    from DebtPositionTypeOrg dpto
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    WHERE dpto.organizationId = :organizationId
    AND dpto.code = :code
    AND dptoo.operatorExternalUserId = :operatorExternalUserId
    """)
  DebtPositionTypeOrg findDebtPositionTypeOrg(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("code") String code,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId
  );

  @Query("""
    SELECT dpto from InstallmentNoPII i
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dpto.debtPositionTypeOrgId = dp.debtPositionTypeOrgId
    WHERE i.nav = :nav
    AND dp.organizationId = :organizationId
    AND dp.debtPositionOrigin IN :debtPositionOrigins
    AND i.status != 'CANCELLED'
    """)
  DebtPositionTypeOrg findDebtPositionTypeOrgByOrgIdAndNavAndOrigins(
    Long organizationId,
    String nav,
    List<DebtPositionOrigin> debtPositionOrigins);

  @Query("""
    SELECT distinct dpto
    from InstallmentNoPII i
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dpto.debtPositionTypeOrgId = dp.debtPositionTypeOrgId
    WHERE dp.organizationId = :organizationId
    AND i.iud IN :iuds
    """)
  List<DebtPositionTypeOrg> findDebtPositionTypeOrgByOrganizationIdAndIuds(
    Long organizationId,
    Set<String> iuds);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE DebtPositionTypeOrg dpto SET dpto.flagActive = :flagActive  WHERE dpto.debtPositionTypeOrgId = :debtPositionTypeOrgId")
  int updateFlagActiveDebtPositionTypeOrg(Long debtPositionTypeOrgId, boolean flagActive);

  @Query
    ("""
      SELECT COUNT(d)
      FROM DebtPositionTypeOrg d
      WHERE d.notifyOutcomePushOrgSilServiceId = :orgSilServiceId
      OR d.amountActualizationOrgSilServiceId = :orgSilServiceId
      """)
  long countByOrgSilServiceId(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("orgSilServiceId") Long orgSilServiceId);

  @Query("""
    SELECT dpto
    FROM DebtPositionTypeOrg dpto
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    WHERE dpto.organizationId = :organizationId
    AND dptoo.operatorExternalUserId = :operatorExternalUserId
    AND (:code IS NULL OR dpto.code = :code)
    AND (:description IS NULL OR dpto.description ILIKE CONCAT('%', cast(:description as text), '%'))
    AND (:debtPositionTypeId IS NULL OR dpto.debtPositionTypeId = :debtPositionTypeId)
    """)
  Page<DebtPositionTypeOrg> findPagedDebtPositionTypeOrg(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Param("code") String code,
    @Param("description") String description,
    @Parameter(schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionTypeId") Long debtPositionTypeId,
    Pageable pageable);

}

