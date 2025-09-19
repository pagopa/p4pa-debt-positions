package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "receipts")
public interface  ReceiptNoPIIRepository extends JpaRepository<ReceiptNoPII,Long> {

  @RestResource(exported = false)
  @Query(" select r" +
    "  from ReceiptNoPII r" +
    " where r.paymentReceiptId = :paymentReceiptId")
  ReceiptNoPII getByPaymentReceiptId(@Param("paymentReceiptId") String paymentReceiptId);

  @Query("""
    SELECT r
    FROM ReceiptNoPII r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN Transfer t ON t.installmentId = i.installmentId
    WHERE t.transferId = :transferId
  """)
  ReceiptNoPII getByTransferId(Long transferId);

  @Query("""
    SELECT r
    FROM ReceiptNoPII r
    JOIN InstallmentNoPII i ON r.receiptId = i.receiptId
    JOIN PaymentOption po ON i.paymentOptionId = po.paymentOptionId
    JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId
    JOIN DebtPositionTypeOrg dpto ON dpto.debtPositionTypeOrgId = dp.debtPositionTypeOrgId
    WHERE r.receiptId = :receiptId
    AND dpto.code = :debtPositionTypeOrgCode
  """)
  ReceiptNoPII getByReceiptIdAndDebtPositionTypeOrgCode(Long receiptId, String debtPositionTypeOrgCode);
}
