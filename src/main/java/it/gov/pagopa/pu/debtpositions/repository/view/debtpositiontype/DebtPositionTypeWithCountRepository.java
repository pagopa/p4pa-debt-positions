package it.gov.pagopa.pu.debtpositions.repository.view.debtpositiontype;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.model.view.debtpositiontype.DebtPositionTypeWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-position-types-with-count")
public interface DebtPositionTypeWithCountRepository extends Repository<DebtPositionTypeWithCount, Long> {

  @Query("""
    SELECT d
    FROM DebtPositionTypeWithCount d
    WHERE d.brokerId = :brokerId
    AND (:code IS NULL OR d.code ILIKE CONCAT('%', CAST(:code AS text), '%'))
    AND (:description IS NULL OR d.description ILIKE CONCAT('%', cast(:description as text), '%'))
    """)
  Page<DebtPositionTypeWithCount> findByBrokerId(
    @Param("brokerId") @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) Long brokerId,
    String code,
    String description,
    Pageable pageable);

}
