package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontypeorg;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontypeorg.DebtPositionTypeOrgWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.RequestParam;

@RepositoryRestResource(path = "debt-position-type-orgs-with-count")
public interface DebtPositionTypeOrgWithCountRepository extends Repository<DebtPositionTypeOrgWithCount, Long> {

  @Query("""
    SELECT d
    FROM DebtPositionTypeOrgWithCount d
    WHERE d.organizationId = :organizationId
    AND (:code IS NULL OR d.code ILIKE CONCAT('%', CAST(:code AS text), '%'))
    AND (:description IS NULL OR d.description ILIKE CONCAT('%', CAST(:description AS text), '%'))
    AND (:flagActive IS NULL OR d.flagActive= :flagActive)
    """)
  Page<DebtPositionTypeOrgWithCount> findByCodeAndDescription(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @RequestParam(required = false) @Param("code") String code,
    @RequestParam(required = false) @Param("description") String description,
    @RequestParam(required = false) @Param("flagActive")  Boolean flagActive,
    Pageable pageable);

}
