package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptView;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "receipts-view")
public interface ReceiptViewRepository extends Repository<ReceiptView, Long> {

  @SuppressWarnings("squid:S107") // Suppressing too many parameters warning: it's allowed in query methods
  @Query(value = "SELECT new ReceiptView(r.receiptId as receiptId, r.paymentAmountCents as paymentAmountCents,r.paymentDateTime as paymentDateTime, i.installmentId as installmentId, r.receiptOrigin as receiptOrigin,i.iuv as iuv, dpto.description as debtPositionTypeOrgDescription, r.debtorFiscalCodeHash as debtorFiscalCodeHash, i.iud as iud) "
    + "FROM ReceiptView r "
    + "JOIN InstallmentNoPII i ON r.receiptId = i.receiptId "
    + "JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId "
    + "JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId "
    + "JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId "
    + "JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId "
    + "WHERE dp.organizationId = :organizationId "
    + "AND dptoo.operatorExternalUserId = :operatorExternalUserId "
    + "AND (:receiptOrigins IS NULL OR r.receiptOrigin IN :receiptOrigins) "
    + "AND (:iuv IS NULL OR i.iuv = :iuv) "
    + "AND (:iur IS NULL OR i.iur = :iur) "
    + "AND (:iud IS NULL OR i.iud = :iud) "
    + "AND (:debtPositionTypeOrgId IS NULL OR dp.debtPositionTypeOrgId = :debtPositionTypeOrgId) "
    + "AND (cast(:paymentDateTimeFrom as date) IS NULL OR r.paymentDateTime >= :paymentDateTimeFrom) "
    + "AND (cast(:paymentDateTimeTo as date) IS NULL OR r.paymentDateTime <= :paymentDateTimeTo) "
    + "AND ((:fiscalCode IS NULL) OR (r.debtorFiscalCodeHash = :#{@dataCipherService.hash(#fiscalCode)})) "
    + "AND dpto.code <> :#{T(it.gov.pagopa.pu.debtpositions.util.Constants).MIXED_DP_TYPE_ORG_CODE} ")
  Page<ReceiptView> findReceiptsByFilters(
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Param("receiptOrigins") List<ReceiptOriginType> receiptOrigins,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Param("iuv") String iuv,
    @Param("iur") String iur,
    @Param("iud") String iud,
    @Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId,
    @Param("paymentDateTimeFrom") OffsetDateTime fromDate,
    @Param("paymentDateTimeTo") OffsetDateTime toDate,
    String fiscalCode,
    Pageable pageable);

}
