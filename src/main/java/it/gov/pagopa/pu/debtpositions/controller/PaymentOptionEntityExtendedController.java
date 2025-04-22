package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.PaymentOptionEntityExtendedControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Controller to host spring-data-rest directly not supported methods */
@RestController
public class PaymentOptionEntityExtendedController implements PaymentOptionEntityExtendedControllerApi {

  private final PaymentOptionRepository repository;

  public PaymentOptionEntityExtendedController(PaymentOptionRepository repository) {
    this.repository = repository;
  }

  @Override
  public ResponseEntity<Void> updateStatus(Long paymentOptionId, PaymentOptionStatus status) {
    repository.updateStatus(paymentOptionId, status);
    return ResponseEntity.ok().build();
  }
}
