package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.dto.filters.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface InstallmentViewNoPIIRepository extends Repository<InstallmentViewNoPII, Long> {

  @RestResource(exported = false)
  @Query("""
    SELECT new InstallmentViewNoPII(
    i.installmentId as installmentId,
    dp.debtPositionId as debtPositionId,
    i.paymentOptionId as paymentOptionId,
    i.receiptId as receiptId,
    i.iuv as iuv,
    i.iud as iud,
    i.status as status,
    i.nav as nav,
    i.dueDate as dueDate,
    i.amountCents as amountCents,
    i.remittanceInformation as remittanceInformation,
    i.debtorFiscalCodeHash as debtorFiscalCodeHash,
    i.personalDataId as personalDataId,
    dpto.description as debtPositionTypeOrgDescription
    )
    FROM InstallmentViewNoPII i
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    WHERE dp.organizationId = :#{#filter.organizationId}
    AND dptoo.operatorExternalUserId = :#{#filter.operatorExternalUserId}
    AND (CAST(:#{#filter.dueDateFrom} AS STRING) IS NULL OR i.dueDate >= :#{#filter.dueDateFrom})
    AND (CAST(:#{#filter.dueDateTo} AS STRING) IS NULL OR i.dueDate <= :#{#filter.dueDateTo})
    AND (:#{#filter.iuv} IS NULL OR i.iuv = :#{#filter.iuv})
    AND (:#{#filter.iud} is NULL OR i.iud = :#{#filter.iud})
    AND ((:#{#filter.fiscalCode} IS NULL) OR i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#filter.fiscalCode)})
    AND (:#{#filter.debtPositionOrigins} IS NULL OR dp.debtPositionOrigin IN (:#{#filter.debtPositionOrigins}))
    AND ((:#{#filter.debtPositionTypeOrgId} IS NULL) OR (dpto.debtPositionTypeOrgId = :#{#filter.debtPositionTypeOrgId} ))
    AND ((:#{#filter.status} IS NULL) OR (i.status = :#{#filter.status} ))
    """)
  Page<InstallmentViewNoPII> findInstallmentsByFilters(
    @Parameter(required = true) @Param("filter") InstallmentsSearchFiltersDTO installmentsSearchFiltersDTO,
    Pageable pageable);

}
