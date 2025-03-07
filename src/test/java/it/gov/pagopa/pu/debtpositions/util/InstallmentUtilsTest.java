package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InstallmentUtilsTest {

  @Test
  void givenValidTransitionWhenIsTransitionToSyncThenReturnTrue() {
    Assertions.assertTrue(
      InstallmentUtils.isTransitionToSync(InstallmentStatus.DRAFT, InstallmentStatus.UNPAID));
    Assertions.assertTrue(
      InstallmentUtils.isTransitionToSync(InstallmentStatus.UNPAID, InstallmentStatus.INVALID));
    Assertions.assertTrue(
      InstallmentUtils.isTransitionToSync(InstallmentStatus.UNPAID, InstallmentStatus.CANCELLED));
  }

  @Test
  void givenInvalidTransitionWhenIsTransitionToSyncThenReturnFalse() {
    Assertions.assertFalse(
      InstallmentUtils.isTransitionToSync(InstallmentStatus.DRAFT, InstallmentStatus.PAID));
    Assertions.assertFalse(
      InstallmentUtils.isTransitionToSync(InstallmentStatus.PAID, InstallmentStatus.UNPAID));
    Assertions.assertFalse(
      InstallmentUtils.isTransitionToSync(InstallmentStatus.CANCELLED, InstallmentStatus.DRAFT));
  }

  @Test
  void givenNullStatusWhenIsTransitionToSyncThenReturnFalse() {
    Assertions.assertFalse(InstallmentUtils.isTransitionToSync(null, InstallmentStatus.UNPAID));
    Assertions.assertFalse(InstallmentUtils.isTransitionToSync(InstallmentStatus.DRAFT, null));
    Assertions.assertFalse(InstallmentUtils.isTransitionToSync(null, null));
  }

  @Test
  void givenInstallmentWhenUpdateInstallmentFieldsThenStatusUpdated() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    InstallmentUtils.updateInstallmentFields(installment, InstallmentStatus.PAID);
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
  }

  @Test
  void givenInstallmentTransitionToSyncWhenUpdateInstallmentFieldsThenSyncStatusUpdated() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.DRAFT);
    InstallmentUtils.updateInstallmentFields(installment, InstallmentStatus.UNPAID);
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.DRAFT, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.UNPAID, installment.getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenInstallmentNotTransitionToSyncWhenUpdateInstallmentFieldsThenSyncStatusNull() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    InstallmentUtils.updateInstallmentFields(installment, InstallmentStatus.PAID);
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertNull(installment.getSyncStatus());
  }

  @Test
  void givenInstallmentWithSyncStatusWhenUpdateSyncStatusThenSyncStatusUpdated() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.TO_SYNC);
    InstallmentSyncStatus syncStatus = InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.DRAFT)
      .syncStatusTo(InstallmentStatus.UNPAID)
      .build();
    installment.setSyncStatus(syncStatus);
    InstallmentUtils.updateSyncStatus(installment, InstallmentStatus.INVALID);
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.DRAFT, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.INVALID, installment.getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenInstallmentWithoutSyncStatusWhenUpdateSyncStatusThenSyncStatusUpdated() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    InstallmentUtils.updateSyncStatus(installment, InstallmentStatus.INVALID);
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.UNPAID, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.INVALID, installment.getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenInstallmentNotPaidWhenIsInstallmentNotPaidThenReturnTrue() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    Assertions.assertTrue(InstallmentUtils.isInstallmentNotPaid(installment));
  }

  @Test
  void givenInstallmentPaidWhenIsInstallmentNotPaidThenReturnFalse() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.PAID);
    Assertions.assertFalse(InstallmentUtils.isInstallmentNotPaid(installment));
  }
}
