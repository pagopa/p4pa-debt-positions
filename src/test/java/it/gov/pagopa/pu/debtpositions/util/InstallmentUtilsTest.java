package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InstallmentUtilsTest {
  @Test
  void givenInstallmentWhenUpdateInstallmentFieldsThenStatusUpdated() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);

    // when
    InstallmentUtils.setStatus(installment, InstallmentStatus.PAID);

    // then
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
  }

  @Test
  void givenInstallmentTransitionToSyncWhenUpdateInstallmentFieldsThenSyncStatusUpdated() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.DRAFT);

    // when
    InstallmentUtils.setStatus(installment, InstallmentStatus.UNPAID);

    // then
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.DRAFT, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.UNPAID, installment.getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenInstallmentNotTransitionToSyncWhenUpdateInstallmentFieldsThenSyncStatusNull() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);

    // when
    InstallmentUtils.setStatus(installment, InstallmentStatus.PAID);

    // then
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertNull(installment.getSyncStatus());
  }

  @Test
  void givenInstallmentNotPaidWhenIsInstallmentNotPaidThenReturnTrue() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);

    // when
    boolean result = InstallmentUtils.isPayable(installment);

    // then
    Assertions.assertTrue(result);
  }

  @Test
  void givenInstallmentPaidWhenIsInstallmentNotPaidThenReturnFalse() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.PAID);

    // when
    boolean result = InstallmentUtils.isPayable(installment);

    // then
    Assertions.assertFalse(result);
  }

  @Test
  void givenInstallmentToSyncWithSyncStatusFromUnpaidWhenUpdateInstallmentFieldsThenStatusUpdated() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.TO_SYNC);
    installment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.UNPAID)
      .build());

    // when
    InstallmentUtils.setStatus(installment, InstallmentStatus.INVALID);

    // then
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.UNPAID, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.INVALID, installment.getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenInstallmentExpiredWhenUpdateInstallmentFieldsThenStatusUpdatedWithoutSync() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.EXPIRED);

    // when
    InstallmentUtils.setStatus(installment, InstallmentStatus.PAID);

    // then
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertNull(installment.getSyncStatus());
  }

  @Test
  void givenInstallmentToSyncWithSyncStatusDraftWhenUpdateInstallmentFieldsThenSyncStatusUpdated() {
    // given
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.TO_SYNC);
    installment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.DRAFT)
      .build());

    // when
    InstallmentUtils.setStatus(installment, InstallmentStatus.UNPAID);

    // then
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.DRAFT, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.UNPAID, installment.getSyncStatus().getSyncStatusTo());
  }
}
