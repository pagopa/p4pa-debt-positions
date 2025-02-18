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

import java.util.Optional;

@RepositoryRestResource(path = "payment-options")
public interface PaymentOptionRepository extends JpaRepository<PaymentOption, Long> {

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE PaymentOption p SET p.status = :status WHERE p.paymentOptionId = :paymentOptionId")
  void updateStatus(@Param("paymentOptionId") Long paymentOptionId, @Param("status") PaymentOptionStatus status);

  @Query(value = "SELECT po from PaymentOption po " +
    "JOIN DebtPosition dp ON po.debtPositionId = dp.debtPositionId " +
    "WHERE dp.iupdOrg = :iupdOrg AND " +
    "po.paymentOptionIndex = :paymentOptionIndex")
  Optional<PaymentOption> getByPaymentOptionIndexAndIupdOrg(Integer paymentOptionIndex, String iupdOrg);

  PaymentOption findByPaymentOptionId(Long paymentOptionId);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE PaymentOption p " +
    "SET p.totalAmountCents = :totalAmountCents, p.description = :description " +
    "WHERE p.paymentOptionId = :paymentOptionId")
  void update(@Param("paymentOptionId") Long paymentOptionId, @Param("status") PaymentOptionStatus status,
              @Param("totalAmountCents") Long totalAmountCents, @Param("description") String description);

  @RestResource(exported = false)
  @Transactional
  @Modifying
  @Query("UPDATE PaymentOption p " +
    "SET p.totalAmountCents = :totalAmountCents " +
    "WHERE p.paymentOptionId = :paymentOptionId")
  void updateTotalAmounts(@Param("paymentOptionId") Long paymentOptionId, @Param("totalAmountCents") Long totalAmountCents);

}
