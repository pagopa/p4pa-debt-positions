package it.gov.pagopa.pu.debtpositions.repository.view.debtposition;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.view.debtposition.DebtPositionView;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "debt-positions-view")
public interface DebtPositionViewRepository extends Repository<DebtPositionView,Long> {
  @SuppressWarnings("squid:S107") // Suppressing too many parameters warning: it's allowed in query methods
  @Query("""
    SELECT distinct new DebtPositionView(
      dp.debtPositionId as debtPositionId,
      dp.description as description,
      dpto.description as debtPositionTypeOrgDescription,
      dp.creationDate as creationDate,
      dp.status as status,
      dp.debtPositionOrigin as debtPositionOrigin,
      dpto.debtPositionTypeOrgId as debtPositionTypeOrgId,
      dp.organizationId as organizationId
    )
    FROM DebtPositionView dp
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    JOIN PaymentOption po ON dp.debtPositionId = po.debtPositionId
    JOIN InstallmentNoPII i ON i.paymentOptionId = po.paymentOptionId
    WHERE
      dp.organizationId = :organizationId
      AND (:debtPositionOrigins IS NULL OR dp.debtPositionOrigin IN :debtPositionOrigins)
      AND dptoo.operatorExternalUserId = :operatorExternalUserId
      AND (cast(:creationDateFrom as date) IS NULL OR dp.creationDate >= :creationDateFrom)
      AND (cast(:creationDateTo as date) IS NULL OR dp.creationDate <= :creationDateTo)
      AND ((:fiscalCode IS NULL) OR (i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#fiscalCode)} ))
      AND ((:debtPositionTypeOrgId IS NULL) OR (dpto.debtPositionTypeOrgId = :debtPositionTypeOrgId ))
      AND ((:status IS NULL) OR (dp.status = :status ))
      AND ((:iuv IS NULL) OR (i.iuv = :iuv ))
  """
  )
  Page<DebtPositionView> findDebtPositionViews(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @Parameter(array = @ArraySchema(schema = @Schema(type = "string"))) @Param("debtPositionOrigins") List<DebtPositionOrigin> debtPositionOrigins,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Parameter(schema = @Schema(type = "LocalDateTime")) @Param("creationDateFrom") LocalDateTime creationDateFrom,
    @Parameter(schema = @Schema(type = "LocalDateTime")) @Param("creationDateTo") LocalDateTime creationDateTo,
    String fiscalCode,
    Long debtPositionTypeOrgId,
    DebtPositionStatus status,
    String iuv,
    Pageable pageable
  );
}
