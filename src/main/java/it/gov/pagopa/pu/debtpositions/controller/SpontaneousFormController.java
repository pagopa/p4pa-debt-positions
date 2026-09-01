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
    log.info("creating SpontaneousForm having organizationId {} and code {}", spontaneousForm.getOrganizationId(), spontaneousForm.getCode());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(spontaneousFormService.createSpontaneousForm(spontaneousForm));
  }

  @Override
  public ResponseEntity<Void> deleteSpontaneousForm(Long spontaneousFormId) {
    log.info("deleting SpontaneousForm having spontaneousFormId {}", spontaneousFormId);
    spontaneousFormService.deleteSpontaneousForm(spontaneousFormId);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> updateSpontaneousForm(SpontaneousForm spontaneousForm) {
    log.info("updating SpontaneousForm having organizationId {} and code {}", spontaneousForm.getOrganizationId(), spontaneousForm.getCode());
    spontaneousFormService.updateSpontaneousForm(spontaneousForm);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<SpontaneousForm> resolveSpontaneousForm(SpontaneousForm spontaneousForm) {
    log.info("resolving SpontaneousForm having organizationId {} and code {}", spontaneousForm.getOrganizationId(), spontaneousForm.getCode());
    return ResponseEntity.ok(spontaneousFormService.resolveOrCreateSpontaneousForm(spontaneousForm));
  }
}
