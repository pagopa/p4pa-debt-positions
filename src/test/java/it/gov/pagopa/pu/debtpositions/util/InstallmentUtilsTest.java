package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InstallmentUtilsTest {

  @Test
  void givenInstallmentWhenUpdateInstallmentFieldsThenStatusUpdated() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    InstallmentUtils.setStatus(installment, InstallmentStatus.PAID);
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
  }

  @Test
  void givenInstallmentTransitionToSyncWhenUpdateInstallmentFieldsThenSyncStatusUpdated() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.DRAFT);
    InstallmentUtils.setStatus(installment, InstallmentStatus.UNPAID);
    Assertions.assertEquals(InstallmentStatus.TO_SYNC, installment.getStatus());
    Assertions.assertNotNull(installment.getSyncStatus());
    Assertions.assertEquals(InstallmentStatus.DRAFT, installment.getSyncStatus().getSyncStatusFrom());
    Assertions.assertEquals(InstallmentStatus.UNPAID, installment.getSyncStatus().getSyncStatusTo());
  }

  @Test
  void givenInstallmentNotTransitionToSyncWhenUpdateInstallmentFieldsThenSyncStatusNull() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    InstallmentUtils.setStatus(installment, InstallmentStatus.PAID);
    Assertions.assertEquals(InstallmentStatus.PAID, installment.getStatus());
    Assertions.assertNull(installment.getSyncStatus());
  }

  @Test
  void givenInstallmentNotPaidWhenIsInstallmentNotPaidThenReturnTrue() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.UNPAID);
    Assertions.assertTrue(InstallmentUtils.isPayable(installment));
  }

  @Test
  void givenInstallmentPaidWhenIsInstallmentNotPaidThenReturnFalse() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setStatus(InstallmentStatus.PAID);
    Assertions.assertFalse(InstallmentUtils.isPayable(installment));
  }
}
