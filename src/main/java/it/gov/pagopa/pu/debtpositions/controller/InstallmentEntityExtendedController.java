package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.InstallmentsEntityExtendedControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Controller to host spring-data-rest directly not supported methods */
@RestController
public class InstallmentEntityExtendedController implements InstallmentsEntityExtendedControllerApi {

  private final InstallmentNoPIIRepository repository;

  public InstallmentEntityExtendedController(InstallmentNoPIIRepository repository) {
    this.repository = repository;
  }

  @Override
  public ResponseEntity<Void> updateDueDate(Long installmentId, LocalDate dueDate) {
    repository.updateDueDate(installmentId, dueDate);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> updateStatusAndToSyncStatus(Long installmentId, InstallmentStatus status, InstallmentStatus syncStatusFrom, InstallmentStatus syncStatusTo) {
    InstallmentSyncStatus syncStatus=null;
    if(syncStatusFrom!=null && syncStatusTo!=null){
      syncStatus = new InstallmentSyncStatus(syncStatusFrom, syncStatusTo);
    }
    repository.updateStatusAndToSyncStatus(installmentId, status, syncStatus);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> updateIun(Long installmentId, String iun) {
    repository.updateIun(installmentId, iun);
    return ResponseEntity.ok().build();
  }

}
