package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "installments")
public interface InstallmentNoPIIRepository extends JpaRepository<InstallmentNoPII, Long> {

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.status = :status, i.iupdPagopa = :iupdPagopa, i.syncStatus = NULL WHERE i.installmentId = :installmentId")
  void updateStatusAndIupdPagopa(@Param("installmentId") Long installmentId, @Param("iupdPagopa") String iupdPagopa, @Param("status") InstallmentStatus status);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE InstallmentNoPII i SET i.status = :status WHERE i.installmentId = :installmentId")
  void updateStatus(@Param("installmentId") Long installmentId, @Param("status") InstallmentStatus status);

  @Query("""
    SELECT COUNT(i)
    FROM DebtPosition dp
    JOIN dp.paymentOptions po
    JOIN po.installments i
    WHERE dp.organizationId = :orgId
      AND i.status <> 'CANCELLED'
      AND ((i.iud = :iud) OR (:iuv IS NOT NULL AND i.iuv = :iuv) OR (:nav IS NOT NULL AND i.nav = :nav))
    """)
  long countExistingInstallments(@Param("orgId") Long orgId, @Param("iud") String iud, @Param("iuv") String iuv, @Param("nav") String nav);

  @RestResource(exported = false)
  @Query(" select i" +
          "  from InstallmentNoPII i" +
          "  join PaymentOption po" +
          "    on i.paymentOptionId = po.paymentOptionId" +
          "  join DebtPosition dp" +
          "    on po.debtPositionId = dp.debtPositionId" +
          " where dp.organizationId = :organizationId" +
          "   and (:debtPositionOrigins is null or dp.debtPositionOrigin in (:debtPositionOrigins))" +
          "   and i.nav = :nav")
  List<InstallmentNoPII> getByOrganizationIdAndNav(@Param("organizationId") Long organizationId, @Param("nav") String nav,
                                                   @Param("debtPositionOrigins") List<DebtPositionOrigin> debtPositionOrigins);
}
