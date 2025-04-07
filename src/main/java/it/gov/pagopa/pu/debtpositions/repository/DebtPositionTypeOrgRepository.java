package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "debt-position-type-orgs")
public interface DebtPositionTypeOrgRepository extends JpaRepository<DebtPositionTypeOrg, Long> {

  Optional<DebtPositionTypeOrg> findByOrganizationIdAndDebtPositionTypeOrgId(Long organizationId, Long debtPositionTypeOrgId);

  Optional<DebtPositionTypeOrg> findByOrganizationIdAndCode(Long organizationId, String code);

  @Query(value = "SELECT dpto "
    + "FROM DebtPositionTypeOrg dpto "
    + "JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId "
    + "WHERE dpto.organizationId = :organizationId "
    + "AND dptoo.operatorExternalUserId = :operatorExternalUserId")
  List<DebtPositionTypeOrg> findDebtPositionTypeOrgs(@Parameter(required = true) @Param("organizationId") Long organizationId,
                                                     @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);

  @Query("""
    SELECT dpto
    FROM DebtPositionTypeOrg dpto
    WHERE dpto.debtPositionTypeId = :debtPositionTypeId
    """)
  List<DebtPositionTypeOrg> findByDebtPositionTypeId(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionTypeId") Long debtPositionTypeId);

  @Query("select dpto from InstallmentNoPII i " +
    "join PaymentOption po on i.paymentOptionId = po.paymentOptionId " +
    "join DebtPosition dp on po.debtPositionId = dp.debtPositionId " +
    "join DebtPositionTypeOrg dpto on dpto.debtPositionTypeOrgId = dp.debtPositionTypeOrgId " +
    "where i.installmentId = :installmentId")
  DebtPositionTypeOrg getDebtPositionTypeOrgByInstallmentId(long installmentId);

}
