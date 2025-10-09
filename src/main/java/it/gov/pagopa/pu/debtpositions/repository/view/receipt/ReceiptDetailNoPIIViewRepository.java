package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource(path = "receipts-detail-view")
public interface ReceiptDetailNoPIIViewRepository extends Repository<ReceiptDetailNoPIIView, Long> {
  @RestResource(exported = false)
  @Query(value = "SELECT new ReceiptDetailNoPIIView("
      + "r.receiptId as receiptId, "
      + "i.iuv as iuv, "
      + "r.paymentAmountCents as paymentAmountCents, "
      + "i.remittanceInformation as remittanceInformation, "
      + "dpto.description as debtPositionTypeOrgDescription, "
      + "i.personalDataId as debtorPersonalDataId, "
      + "r.paymentDateTime as paymentDateTime, "
      + "r.pspCompanyName as pspCompanyName, "
      + "i.iud as iud, "
      + "i.iur as iur, "
      + "r.feeCents as feeCents, "
      + "i.notificationFeeCents as notificationFeeCents "
    + ") "
    + "FROM ReceiptDetailNoPIIView r "
    + "JOIN InstallmentNoPII i ON r.receiptId = i.receiptId "
    + "JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId "
    + "JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId "
    + "JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId "
    + "JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId "
    + "WHERE r.receiptId = :receiptId "
    + "AND dptoo.operatorExternalUserId = :operatorExternalUserId "
    + "AND dp.organizationId = :organizationId "
    + "AND dp.debtPositionOrigin IN (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).ORDINARY_DEBT_POSITION_ORIGINS})")
  Optional<ReceiptDetailNoPIIView> findReceiptDetailView(
    @Parameter(required = true) @Param("receiptId") Long receiptId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Parameter(required = true) @Param("organizationId") Long organizationId);
}
