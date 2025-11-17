package it.gov.pagopa.pu.debtpositions.repository.view.debtposition;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.model.view.debtposition.DebtPositionExtendedView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "debt-position-extended-view")
public interface DebtPositionExtendedViewRepository extends Repository<DebtPositionExtendedView, Long> {

  @Query("""
   SELECT distinct new DebtPositionExtendedView(
      dp.debtPositionId as debtPositionId,
      dp.description as description,
      dpto.description as debtPositionTypeOrgDescription,
      dp.creationDate as creationDate,
      dp.status as status,
      dp.debtPositionOrigin as debtPositionOrigin,
      dpto.debtPositionTypeOrgId as debtPositionTypeOrgId,
      dp.organizationId as organizationId,
      dpt.description as debtPositionTypeDescription,
      dpt.taxonomyCode as taxonomyCode
    )
    FROM DebtPosition dp
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionType dpt ON dpto.debtPositionTypeId = dpt.debtPositionTypeId
    WHERE
     dp.status IN (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).OPEN_DP_STATUSES})
     AND ((:organizationIds is NULL OR dp.organizationId IN :organizationIds))
     AND EXISTS (
             SELECT 1
             FROM PaymentOption po
             JOIN po.installments i
             WHERE po.debtPositionId = dp.debtPositionId
             AND i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)}
     )
  """)
  Page<DebtPositionExtendedView> findPagedPrimaryDebtPositionExtendViewByFilters(
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    @Parameter(name = "organizationIds", required = true, array = @ArraySchema(schema = @Schema(type = "integer", format = "int64")))@Param("organizationIds") List<Long> organizationIds,
    Pageable pageable
  );
}
