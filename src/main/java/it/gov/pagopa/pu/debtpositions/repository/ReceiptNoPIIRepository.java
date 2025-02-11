package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "receipts")
public interface ReceiptNoPIIRepository extends JpaRepository<ReceiptNoPII,Long> {
  Optional<ReceiptNoPII> findByReceiptIdAndOrgFiscalCode(Long receiptId,String orgFiscalCode);
}
