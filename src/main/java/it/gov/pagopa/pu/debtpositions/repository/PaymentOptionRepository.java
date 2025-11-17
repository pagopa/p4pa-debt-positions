package it.gov.pagopa.pu.debtpositions.repository;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "payment-options")
public interface PaymentOptionRepository extends JpaRepository<PaymentOption,Long> {

  @Transactional
  @Modifying
  @RestResource(exported = false)
  @Query("UPDATE PaymentOption p SET p.status = :status WHERE p.paymentOptionId = :paymentOptionId")
  @ApiResponse(responseCode = "200", description = "Status updated successfully")
  void updateStatus(@Param("paymentOptionId") Long paymentOptionId, @Param("status") PaymentOptionStatus status);

  @Query("""
    SELECT po
    FROM PaymentOption po
    WHERE
     po.debtPositionId = :debtPositionId
     AND po.status IN (:#{T(it.gov.pagopa.pu.debtpositions.util.InstallmentUtils).PAYABLE_PO_STATUSES})
  """)
  List<PaymentOption> findPayablePaymentOptionsByDebtPositionId(@Parameter(required = true, schema = @Schema(type = "integer", format = "int64")) @Param("debtPositionId")Long debtPositionId);

}
