package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptArchivingNoPIIView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.OffsetDateTime;

@RepositoryRestResource(path = "receipt-archiving-export-view")
public interface ReceiptArchivingNoPIIViewRepository extends Repository<ReceiptArchivingNoPIIView, Long> {
    @RestResource(exported = false)
    @Query("""
    SELECT new ReceiptArchivingNoPIIView(
      r.receiptId as receiptId,
      r.paymentReceiptId as paymentReceiptId,
      r.paymentDateTime as paymentDateTime,
      r.creditorReferenceId as creditorReferenceId,
      i.iuv as iuv,
      t.remittanceInformation as remittanceInformation,
      t.orgFiscalCode as orgFiscalCode,
      r.personalDataId as receiptPersonalDataId
    )
    FROM ReceiptNoPII r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN Transfer t ON i.installmentId = t.installmentId
    JOIN DebtPositionTypeOrgOperators dptoo ON dp.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    WHERE
      (i.status = 'PAID' OR i.status = 'REPORTED')
      AND dp.organizationId = :organizationId
      AND r.paymentDateTime BETWEEN :paymentDateTimeFrom AND :paymentDateTimeTo
      AND dptoo.operatorExternalUserId = :operatorExternalUserId
      AND t.transferIndex = 1
  """
    )
    Page<ReceiptArchivingNoPIIView> findReceiptArchivingViewNoPIIDTO(
            @Parameter(required = true) @Param("organizationId") Long organizationId,
            @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
            @Parameter(required = true) @Param("paymentDateTimeFrom") OffsetDateTime paymentDateTimeFrom,
            @Parameter(required = true) @Param("paymentDateTimeTo") OffsetDateTime paymentDateTimeTo,
            Pageable pageable
    );
}
