package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentView;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionOrigin;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface InstallmentViewRepository extends Repository<InstallmentView, Long> {

  // Suppressing too many parameters warning: it's allowed in query methods
  @SuppressWarnings("squid:S107")
  @Query("""
    SELECT new InstallmentView(
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
    dpto.description as debtPositionTypeOrgDescription
    )
    FROM InstallmentView i
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    WHERE dp.organizationId = :organizationId
    AND dptoo.operatorExternalUserId = :operatorExternalUserId
    AND (cast(:dueDateFrom as date) IS NULL OR i.dueDate >= :dueDateFrom)
    AND (cast(:dueDateTo as date) IS NULL OR i.dueDate <= :dueDateTo)
    AND (:iuv IS NULL OR i.iuv = :iuv)
    AND (:iud is NULL or i.iud = :iud)
    AND ((:fiscalCode IS NULL) OR (i.debtorFiscalCodeHash = :#{@dataCipherService.hash(#fiscalCode)} ))
    AND (:debtPositionOrigins IS NULL OR dp.debtPositionOrigin IN (:debtPositionOrigins))
    AND ((:debtPositionTypeOrgId IS NULL) OR (dpto.debtPositionTypeOrgId = :debtPositionTypeOrgId ))
    AND ((:status IS NULL) OR (dp.status = :status ))
    """)
  Page<InstallmentView> findInstallmentsByFilters(
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Parameter(schema = @Schema(type = "string", format = "date")) @Param("dueDateFrom") LocalDate dueDateFrom,
    @Parameter(schema = @Schema(type = "string", format = "date")) @Param("dueDateTo") LocalDate dueDateTo,
    String iuv,
    String iud,
    String fiscalCode,
    List<DebtPositionOrigin> debtPositionOrigins,
    Long debtPositionTypeOrgId,
    InstallmentStatus status,
    Pageable pageable);

}
