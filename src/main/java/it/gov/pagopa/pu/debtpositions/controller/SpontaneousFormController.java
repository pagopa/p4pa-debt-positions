package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.SpontaneousFormApi;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.service.SpontaneousFormService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SpontaneousFormController implements SpontaneousFormApi {

  private final SpontaneousFormService spontaneousFormService;

  public SpontaneousFormController(SpontaneousFormService spontaneousFormService) {
    this.spontaneousFormService = spontaneousFormService;
  }

  @Override
  public ResponseEntity<SpontaneousForm> createSpontaneousForm(SpontaneousForm spontaneousForm) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(spontaneousFormService.createSpontaneousForm(spontaneousForm));
  }
}
