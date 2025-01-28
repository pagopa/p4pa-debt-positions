package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;

public class InstallmentSyncStatusFaker {

  public static InstallmentSyncStatus buildInstallmentSyncStatus() {
    return InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.DRAFT)
      .syncStatusTo(InstallmentStatus.UNPAID)
      .build();
  }
}
