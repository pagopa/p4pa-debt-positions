package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Set;

@RepositoryRestResource(path = "debt-position-types")
public interface DebtPositionTypeRepository extends JpaRepository<DebtPositionType, Long> {

  List<DebtPositionType> findAllByBrokerIdAndOrgType(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("brokerId") Long brokerId,
    @Parameter(required = true) @Param("orgType") String orgType);

@Query("select dpt from DebtPositionType dpt " +
  "where dpt.code = :code " +
  "and dpt.brokerId = :brokerId " +
  "and dpt.orgType = :orgType " +
  "and dpt.macroArea = :macroArea " +
  "and dpt.serviceType = :serviceType " +
  "and dpt.collectingReason = :collectingReason " +
  "and dpt.taxonomyCode = :taxonomyCode")
List<DebtPositionType> findByMainFields(String code, Long brokerId, String orgType, String macroArea, String serviceType, String collectingReason, String taxonomyCode);

List<DebtPositionType> findByBrokerIdAndCode(Long brokerId, String code);

List<DebtPositionType> findByDebtPositionTypeIdIn(Set<Long> debtPositionTypeIds);
}
