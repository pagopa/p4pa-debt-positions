package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.mapper.MixedDebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.ReceiptDetailNoPIIView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "receipts-detail-view")
public interface ReceiptDetailNoPIIViewRepository extends Repository<ReceiptDetailNoPIIView, Long> {
  @RestResource(exported = false)
  @Query("""
    SELECT new ReceiptDetailNoPIIView(
    r.receiptId as receiptId,
    i.iuv as iuv,
    i.nav as nav,
    r.paymentAmountCents as paymentAmountCents,
    i.remittanceInformation as remittanceInformation,
    dpto.description as debtPositionTypeOrgDescription,
    i.personalDataId as debtorPersonalDataId,
    r.paymentDateTime as paymentDateTime,
    r.pspCompanyName as pspCompanyName,
    i.iud as iud,
    i.iur as iur,
    r.feeCents as feeCents,
    i.notificationFeeCents as notificationFeeCents,
    r.receiptOrigin as receiptOrigin,
    dp.debtPositionOrigin as debtPositionOrigin
    )
    FROM ReceiptDetailNoPIIView r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    WHERE r.receiptId = :receiptId
    AND dptoo.operatorExternalUserId = :operatorExternalUserId
    AND dp.organizationId = :organizationId
    AND (:iud IS NULL OR i.iud = :iud)
    AND dpto.code <> :#{T(it.gov.pagopa.pu.debtpositions.util.Constants).MIXED_DP_TYPE_ORG_CODE}
  """)
  List<ReceiptDetailNoPIIView> findReceiptDetailViewInner(
    @Parameter(required = true) @Param("receiptId") Long receiptId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId,
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Param("iud") String iud);

  default Optional<ReceiptDetailNoPIIView> findReceiptDetailView(
    Long receiptId,
    String operatorExternalUserId,
    Long organizationId,
    String iud) {
    List<ReceiptDetailNoPIIView> receipts = findReceiptDetailViewInner(receiptId, operatorExternalUserId, organizationId, iud);
    return resolveMultipleReceipts(receipts);
  }

  private static Optional<ReceiptDetailNoPIIView> resolveMultipleReceipts(List<ReceiptDetailNoPIIView> receipts) {
    if (CollectionUtils.isEmpty(receipts)) {
      return Optional.empty();
    } else if (receipts.size() == 1) {
      return Optional.of(receipts.getFirst());
    } else {
      ReceiptDetailNoPIIView first = receipts.getFirst();
      first.setRemittanceInformation(MixedDebtPositionMapper.MULTIPLE_REMITTANCE_INFO);
      List<String> dpTypes = receipts.stream().map(ReceiptDetailNoPIIView::getDebtPositionTypeOrgDescription).distinct().toList();
      first.setDebtPositionTypeOrgDescription(dpTypes.size() == 1 ? dpTypes.getFirst(): "Tipologie multiple");
      return Optional.of(first);
    }
  }

  @RestResource(exported = false)
  @Query("""
    SELECT new ReceiptDetailNoPIIView(
    r.receiptId as receiptId,
    i.iuv as iuv,
    i.nav as nav,
    r.paymentAmountCents as paymentAmountCents,
    i.remittanceInformation as remittanceInformation,
    dpto.description as debtPositionTypeOrgDescription,
    i.personalDataId as debtorPersonalDataId,
    r.paymentDateTime as paymentDateTime,
    r.pspCompanyName as pspCompanyName,
    i.iud as iud,
    i.iur as iur,
    r.feeCents as feeCents,
    i.notificationFeeCents as notificationFeeCents,
    r.receiptOrigin as receiptOrigin,
    dp.debtPositionOrigin as debtPositionOrigin
    )
    FROM ReceiptDetailNoPIIView r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    WHERE r.receiptId = :receiptId
    AND dp.organizationId = :organizationId
    AND (:iud IS NULL OR i.iud = :iud)
    AND dpto.code <> :#{T(it.gov.pagopa.pu.debtpositions.util.Constants).MIXED_DP_TYPE_ORG_CODE}
  """)
  List<ReceiptDetailNoPIIView> findReceiptDetailViewInner(
    @Parameter(required = true) @Param("receiptId") Long receiptId,
    @Parameter(required = true) @Param("organizationId") Long organizationId,
    @Param("iud") String iud);

  default Optional<ReceiptDetailNoPIIView> findReceiptDetailView(
    Long receiptId,
    Long organizationId,
    String iud) {
    List<ReceiptDetailNoPIIView> receipts = findReceiptDetailViewInner(receiptId, organizationId, iud);
    return resolveMultipleReceipts(receipts);
  }
}
