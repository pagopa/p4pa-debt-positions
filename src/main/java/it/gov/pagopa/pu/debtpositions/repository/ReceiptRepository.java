package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "receipts")
public interface ReceiptRepository extends JpaRepository<ReceiptNoPII,Long> {

}
