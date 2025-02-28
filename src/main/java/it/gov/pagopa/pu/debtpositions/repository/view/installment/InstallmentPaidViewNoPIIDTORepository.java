package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.OffsetDateTime;

@RepositoryRestResource(path = "installment-paid-export-view")
public interface InstallmentPaidViewNoPIIDTORepository extends Repository<InstallmentPaidViewNoPII, Long> {
  @RestResource(exported = false)
  @Query("""
    SELECT new InstallmentPaidViewNoPII(
      i.installmentId as installmentId,
      i.iuf as iuf,
      i.iud as iud,
      r.noticeNumber as noticeNumber,
      r.orgFiscalCode as orgFiscalCode,
      r.paymentReceiptId as paymentReceiptId,
      r.paymentDateTime as paymentDateTime,
      r.idPsp as idPsp,
      r.pspCompanyName as pspCompanyName,
      i.debtorEntityType as debtorEntityType,
      r.paymentAmountCents as paymentAmountCents,
      r.creditorReferenceId as creditorReferenceId,
      t.amountCents as amountCents,
      t.remittanceInformation as remittanceInformation,
      t.category as category,
      dpto.code as code,
      t.transferIndex as transferIndex,
      r.feeCents as feeCents,
      i.balance as balance,
      r.companyName as companyName,
      i.personalDataId as personalDataId
    )
    FROM InstallmentNoPII i
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN ReceiptNoPII r ON i.receiptId = r.receiptId
    JOIN Transfer t ON i.installmentId = t.installmentId
    JOIN DebtPositionTypeOrgOperators dptoo ON dp.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    WHERE
      (i.status = 'PAID' OR i.status = 'REPORTED')
      AND dp.organizationId = :organizationId
      AND r.paymentDateTime BETWEEN :paymentDateTimeFrom AND :paymentDateTimeTo
      AND dptoo.operatorExternalUserId = :operatorExternalUserId
      AND (:debtPositionTypeOrgId IS NULL OR dp.debtPositionTypeOrgId = :debtPositionTypeOrgId)
      AND (:debtPositionTypeOrgId IS NULL OR dptoo.debtPositionTypeOrgId = :debtPositionTypeOrgId)
      AND t.transferIndex = 1
  """
  )
  Page<InstallmentPaidViewNoPII> findInstallmentPaidViewNoPIIDTO(
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Parameter(required = true) @Param("paymentDateTimeFrom") OffsetDateTime paymentDateTimeFrom,
    @Parameter(required = true) @Param("paymentDateTimeTo") OffsetDateTime paymentDateTimeTo,
    @Param("debtPositionTypeOrgId") Long debtPositionTypeOrgId,
    Pageable pageable
  );
}
