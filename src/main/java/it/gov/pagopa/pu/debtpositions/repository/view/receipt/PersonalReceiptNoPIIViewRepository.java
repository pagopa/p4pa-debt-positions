package it.gov.pagopa.pu.debtpositions.repository.view.receipt;

import io.swagger.v3.oas.annotations.Parameter;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.view.receipt.PersonalReceiptNoPIIView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "personal-receipt-view")
public interface PersonalReceiptNoPIIViewRepository extends Repository<PersonalReceiptNoPIIView, Long> {

  @Query("""
    SELECT new PersonalReceiptNoPIIView(
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
      AND (r.orgFiscalCode IN :organizationsFiscalCode)
      AND ((:receiptOrigins IS NULL) OR (r.receiptOrigin IN :receiptOrigins))
   """
  )
  Page<PersonalReceiptNoPIIView> getPagedPersonalReceipt(
    @Parameter(required = true) @Param("debtorFiscalCode") String debtorFiscalCode,
    @Parameter(required = true) @Param("organizationsFiscalCode") List<String> organizationsFiscalCode,
    @Param("receiptOrigins") List<ReceiptOriginType> receiptOrigins,
    Pageable pageable
  );

}
