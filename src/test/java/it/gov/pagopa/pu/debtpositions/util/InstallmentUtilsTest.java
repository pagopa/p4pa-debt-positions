package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
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
}
