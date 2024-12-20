package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "installments")
public interface InstallmentRepository extends JpaRepository<InstallmentNoPII,Long> {

}
