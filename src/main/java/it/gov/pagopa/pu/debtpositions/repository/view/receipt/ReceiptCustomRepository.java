package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.OffsetDateTime;

@RepositoryRestResource(path = "receipts-custom")
public interface ReceiptCustomRepository extends Repository<ReceiptCustom, Long> {

  @SuppressWarnings("squid:S107") // Suppressing too many parameters warning: it's allowed in query methods
  @Query(value = "SELECT new ReceiptCustom(r.receiptId as receiptId, r.paymentAmountCents as paymentAmountCents,r.paymentDateTime as paymentDateTime, i.installmentId as installmentId, r.receiptOrigin as receiptOrigin,i.iuv as iuv, dp.description as description) "
    + "FROM ReceiptCustom r "
    + "JOIN InstallmentNoPII i ON r.installmentId = i.installmentId "
    + "JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId "
    + "JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId "
    + "JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId "
    + "JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId "
    + "WHERE dp.organizationId = :organizationId "
    + "AND dptoo.operatorExternalUserId = :operatorExternalUserId "
    + "AND r.receiptOrigin = :receiptOrigin "
    + "AND (:iuv IS NULL OR i.iuv = :iuv) "
    + "AND (:iur IS NULL OR i.iur = :iur) "
    + "AND (:iud IS NULL OR i.iud = :iud) "
    + "AND (:debtPositionTypeOrgId IS NULL OR dp.debtPositionTypeOrgId = :debtPositionTypeOrgId) "
    + "AND ((cast(:fromDate as date) IS NULL AND cast(:toDate as date) IS NULL) "
    + "OR (cast(:fromDate as date) IS NOT NULL AND cast(:toDate as date) IS NOT NULL "
    + "AND r.paymentDateTime BETWEEN :fromDate AND :toDate))")
  Page<ReceiptCustom> findReceiptsByFilters(
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("receiptOrigin") String receiptOrigin,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Param("iuv") String iuv,
    @Param("iur") String iur,
    @Param("iud") String iud,
    @Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId,
    @Param("fromDate") OffsetDateTime fromDate,
    @Param("toDate") OffsetDateTime toDate,
    Pageable pageable);

}
