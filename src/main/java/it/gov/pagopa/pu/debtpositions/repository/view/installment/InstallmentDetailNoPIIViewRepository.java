package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentDetailNoPIIView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource(path = "installments-detail-view")
public interface InstallmentDetailNoPIIViewRepository extends Repository<InstallmentDetailNoPIIView, Long> {

  @RestResource(exported = false)
  @Query(
    """
    SELECT new InstallmentDetailNoPIIView(
    i.installmentId as installmentId,
    i.receiptId as receiptId,
    i.paymentOptionId as paymentOptionId,
    i.status as status,
    i.iuv as iuv,
    i.amountCents as amountCents,
    i.dueDate as dueDate,
    i.personalDataId as personalDataId,
    dpto.description as debtPositionTypeOrgDescription,
    dp.description as debtPositionDescription,
    dp.debtPositionId as debtPositionId,
    r.paymentDateTime as paymentDateTime,
    r.personalDataId as receiptPersonalDataId,
    r.pspCompanyName as pspCompanyName,
    i.iud as iud,
    i.iur as iur
    )
    FROM InstallmentDetailNoPIIView i
    LEFT JOIN ReceiptNoPII r ON i.receiptId = r.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dp.debtPositionTypeOrgId = dpto.debtPositionTypeOrgId
    JOIN DebtPositionTypeOrgOperators dptoo ON dpto.debtPositionTypeOrgId = dptoo.debtPositionTypeOrgId
    WHERE i.installmentId = :installmentId
    AND dptoo.operatorExternalUserId = :operatorExternalUserId
    """)
  Optional<InstallmentDetailNoPIIView> findInstallmentDetailView(
    @Parameter(required = true) @Param("installmentId") Long installmentId,
    @Parameter(required = true) @Param("operatorExternalUserId") String operatorExternalUserId);

}
