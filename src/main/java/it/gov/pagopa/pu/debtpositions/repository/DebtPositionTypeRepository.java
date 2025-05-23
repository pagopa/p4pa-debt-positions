package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "debt-position-types")
public interface DebtPositionTypeRepository extends JpaRepository<DebtPositionType,Long> {

  List<DebtPositionType> findAllByBrokerIdAndOrgType(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("brokerId") Long brokerId,
    @Parameter(required = true) @Param("orgType") String orgType);

  List<DebtPositionType> findByCodeAndBrokerIdAndOrgTypeAndMacroAreaAndServiceTypeAndCollectingReasonAndTaxonomyCode
    (String code, Long brokerId, String orgType, String macroArea, String serviceType, String collectingReason, String taxonomyCode);
}
