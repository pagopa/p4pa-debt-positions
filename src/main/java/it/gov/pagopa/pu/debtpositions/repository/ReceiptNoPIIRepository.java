package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "receipts")
public interface ReceiptNoPIIRepository extends JpaRepository<ReceiptNoPII,Long> {

  @RestResource(exported = false)
  @Query(" select r" +
    "  from ReceiptNoPII r" +
    " where r.paymentReceiptId = :paymentReceiptId")
  ReceiptNoPII getByPaymentReceiptId(@Param("paymentReceiptId") String paymentReceiptId);

  @Query("""
    SELECT r
    FROM ReceiptNoPII r
    JOIN Installment i ON r.receiptId = i.receiptId
    JOIN Transfer t ON t.installmentId = i.installmentId
    WHERE t.transferId = :transferId
  """)
  ReceiptNoPII getByTransferId(@Param("transferId") Long transferId);
}
