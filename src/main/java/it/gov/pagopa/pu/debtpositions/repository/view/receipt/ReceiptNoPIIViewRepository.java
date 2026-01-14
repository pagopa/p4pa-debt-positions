package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.pu.classification.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptNoPIIView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.OffsetDateTime;
import java.util.List;

@RepositoryRestResource(path = "receipt-no-pii-view")
public interface ReceiptNoPIIViewRepository extends Repository<ReceiptNoPIIView, Long> {

  @Query("""
    SELECT new ReceiptNoPIIView(
      r.receiptId as receiptId,
      r.orgFiscalCode as orgFiscalCode,
      r.paymentAmountCents as paymentAmountCents,
      r.paymentDateTime as paymentDateTime,
      r.receiptOrigin as receiptOrigin,
      i.installmentId as installmentId,
      i.remittanceInformation as remittanceInformation,
      dpto.description as debtPositionTypeOrgDescription,
      dpt.description as debtPositionTypeDescription,
      dpt.serviceType as serviceType
    )
    FROM ReceiptNoPII r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionType dpt ON dpto.debtPositionTypeId = dpt.debtPositionTypeId
    WHERE
      (r.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)})
      AND dp.debtPositionOrigin in (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PRIMARY_ORG_DEBT_POSITION_ORIGINS_NO_MIXED})
      AND (r.orgFiscalCode IN :organizationsFiscalCode)
      AND ((:receiptOrigins IS NULL) OR (r.receiptOrigin IN :receiptOrigins))
      AND ((:noticeNumberOrIuv IS NULL) OR (r.noticeNumber = :noticeNumberOrIuv OR i.iuv = :noticeNumberOrIuv))
      AND (cast(:paymentDateTimeFrom as date) IS NULL OR r.paymentDateTime >= :paymentDateTimeFrom)
      AND (cast(:paymentDateTimeTo as date) IS NULL OR r.paymentDateTime <= :paymentDateTimeTo)
   """
  )
  Page<ReceiptNoPIIView> getPagedPrimaryReceiptByFilters(
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    @Parameter(required = true, array = @ArraySchema(schema = @Schema(type = "string"))) @Param("organizationsFiscalCode") List<String> organizationsFiscalCode,
    @Param("receiptOrigins") List<ReceiptOriginType> receiptOrigins,
    @Param("noticeNumberOrIuv") String noticeNumberOrIuv,
    @Param("paymentDateTimeFrom") OffsetDateTime paymentDateTimeFrom,
    @Param("paymentDateTimeTo") OffsetDateTime paymentDateTimeTo,
    Pageable pageable
  );

  @Query("""
    SELECT new ReceiptNoPIIView(
      r.receiptId as receiptId,
      r.orgFiscalCode as orgFiscalCode,
      r.paymentAmountCents as paymentAmountCents,
      r.paymentDateTime as paymentDateTime,
      r.receiptOrigin as receiptOrigin,
      i.installmentId as installmentId,
      i.remittanceInformation as remittanceInformation,
      dpto.description as debtPositionTypeOrgDescription,
      dpt.description as debtPositionTypeDescription,
      dpt.serviceType as serviceType
    )
    FROM ReceiptNoPII r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionType dpt ON dpto.debtPositionTypeId = dpt.debtPositionTypeId
    WHERE
      (r.debtorFiscalCodeHash = :#{@dataCipherService.hash(#debtorFiscalCode)})
      AND dp.debtPositionOrigin in (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PRIMARY_ORG_DEBT_POSITION_ORIGINS_NO_MIXED})
      AND ((:receiptOrigins IS NULL) OR (r.receiptOrigin IN :receiptOrigins))
      AND ((:installmentStatuses IS NULL) OR (i.status IN :installmentStatuses))
      and dp.organizationId = :organizationId
      AND po.paymentOptionId = :paymentOptionId
      AND dp.debtPositionId = :debtPositionId
   """
  )
  List<ReceiptNoPIIView> getDebtorReceipts(
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("organizationId") Long organizationId,
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionId") Long debtPositionId,
    @Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("paymentOptionId") Long paymentOptionId,
    @Param("receiptOrigins") List<ReceiptOriginType> receiptOrigins,
    @Param("installmentStatuses") List<InstallmentStatus> installmentStatuses
  );
}
