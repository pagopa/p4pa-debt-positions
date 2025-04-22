package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;

import java.io.Serializable;

public interface BaseInstallment extends Serializable {
  Long getInstallmentId();
  String getIud();

  InstallmentStatus getStatus();
  void setStatus(InstallmentStatus status);

  InstallmentSyncStatus getSyncStatus();
  void setSyncStatus(InstallmentSyncStatus syncStatus);
}
