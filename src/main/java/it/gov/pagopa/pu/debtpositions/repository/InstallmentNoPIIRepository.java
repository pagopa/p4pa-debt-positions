package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "installments")
public interface InstallmentNoPIIRepository extends JpaRepository<InstallmentNoPII, Long> {

  InstallmentNoPII findByIudAndIupdPagopaAndStatus(String iud, String iupdPagopa, String status);

  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.status = :status WHERE i.installmentId = :installmentId")
  void updateStatus(@Param("installmentId") Long installmentId, @Param("status") String status);

}
