package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import java.util.Set;

public class InstallmentUtils {

  private InstallmentUtils() {
  }

  private static final Set<String> TRANSITION_TO_SYNC_REQUIRED = Set.of(
    InstallmentStatus.DRAFT + "|" + InstallmentStatus.UNPAID,
    InstallmentStatus.UNPAID + "|" + InstallmentStatus.INVALID,
    InstallmentStatus.UNPAID + "|" + InstallmentStatus.CANCELLED);

  private static final Set<InstallmentStatus> NOT_PAID = Set.of(
    InstallmentStatus.DRAFT,
    InstallmentStatus.TO_SYNC,
    InstallmentStatus.UNPAID,
    InstallmentStatus.EXPIRED);


  private static boolean isTransitionToSync(InstallmentStatus statusFrom,
    InstallmentStatus statusTo) {
    return TRANSITION_TO_SYNC_REQUIRED.contains(statusFrom + "|" + statusTo);
  }

  public static void setStatus(InstallmentNoPII installment, InstallmentStatus status) {
    if (InstallmentUtils.isTransitionToSync(installment.getStatus(), status)) {
      updateSyncStatus(installment, status);
    } else {
      installment.setStatus(status);
      installment.setSyncStatus(null);
    }

  }

  /** It will check if the Installment is in a payable status */
  public static boolean isPayable(InstallmentNoPII installment) {
    return NOT_PAID.contains(installment.getStatus());
  }

  private static void updateSyncStatus(InstallmentNoPII installment,
    InstallmentStatus to) {
    InstallmentStatus from =
      installment.getStatus().equals(InstallmentStatus.TO_SYNC)
        ? installment.getSyncStatus().getSyncStatusFrom()
        : installment.getStatus();
    installment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(from)
      .syncStatusTo(to)
      .build()
    );
    installment.setStatus(InstallmentStatus.TO_SYNC);
  }
}
