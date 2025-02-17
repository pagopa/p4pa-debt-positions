package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
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

  @Query(value = "SELECT dpto "
    + "FROM DebtPositionTypeOrg dpto "
    + "JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId "
    + "WHERE dptoo.debtPositionTypeOrgId = :debtPositionTypeOrgId "
    + "AND dptoo.operatorExternalUserId = :operatorExternalUserId")
  Optional<DebtPositionTypeOrg> findByDebtPositionTypeOrgIdAndOperatorExternalUserId(
    @Parameter(required = true) @Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);
}
