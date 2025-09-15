package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.dto.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "installment-paid-export-view")
public interface InstallmentPaidViewNoPIIDTORepository extends Repository<InstallmentPaidViewNoPII, Long> {
  @RestResource(exported = false)
  @Query("""
    SELECT new InstallmentPaidViewNoPII(
      i.installmentId as installmentId,
      i.sourceFlowName as iuf,
      i.iud as iud,
      r.noticeNumber as noticeNumber,
      dp.organizationId as organizationId,
      r.orgFiscalCode as orgFiscalCode,
      r.paymentReceiptId as paymentReceiptId,
      r.paymentDateTime as paymentDateTime,
      r.idPsp as idPsp,
      r.pspCompanyName as pspCompanyName,
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
      r.personalDataId as receiptPersonalDataId,
      r.rtFilePath as rtFilePath,
      i.iun as iun,
      i.notificationDate as notificationDate,
      i.notificationFeeCents as notificationFeeCents
    )
    FROM InstallmentNoPII i
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN ReceiptNoPII r ON i.receiptId = r.receiptId
    JOIN Transfer t ON i.installmentId = t.installmentId
    JOIN DebtPositionTypeOrgOperators dptoo ON dp.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    WHERE
      i.status in (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PAID_STATUSES})
      AND dp.organizationId = :#{#filter.organizationId}
      AND (CAST(:#{#filter.paymentDateTime.from} AS STRING) IS NULL OR r.paymentDateTime >= :#{#filter.paymentDateTime.from})
      AND (CAST(:#{#filter.paymentDateTime.to} AS STRING) IS NULL OR r.paymentDateTime <= :#{#filter.paymentDateTime.to})
      AND (CAST(:#{#filter.installmentUpdateDateTime.from} AS STRING) IS NULL OR i.updateDate >= :#{#filter.installmentUpdateDateTime.from})
      AND (CAST(:#{#filter.installmentUpdateDateTime.to} AS STRING) IS NULL OR i.updateDate <= :#{#filter.installmentUpdateDateTime.to})
      AND dptoo.operatorExternalUserId = :#{#filter.operatorExternalUserId}
      AND (:#{#filter.debtPositionTypeOrgId} IS NULL OR dptoo.debtPositionTypeOrgId = :#{#filter.debtPositionTypeOrgId})
      AND t.transferIndex = 1
  """
  )
  Page<InstallmentPaidViewNoPII> findInstallmentPaidViewNoPIIDTO(
    @Parameter(required = true) @Param("filter") ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO,
    Pageable pageable
  );
}
