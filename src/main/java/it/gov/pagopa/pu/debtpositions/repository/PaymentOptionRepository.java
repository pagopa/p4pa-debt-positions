package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(path = "payment-options")
public interface PaymentOptionRepository extends JpaRepository<PaymentOption,Long> {

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE PaymentOption p SET p.status = :status WHERE p.paymentOptionId = :paymentOptionId")
  void updateStatus(@Param("paymentOptionId") Long paymentOptionId, @Param("status") PaymentOptionStatus status);

}
