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

  /** It will check if the Installment is in a payable status */
  public static boolean isPayable(InstallmentNoPII installment) {
    return NOT_PAID.contains(installment.getStatus());
  }

  /**
   * It will set the Installment status verifying it will need the TO_SYNC transition
   *
   * @param installment the installment to update
   * @param status the new status
   */
  public static void setStatus(InstallmentNoPII installment, InstallmentStatus status) {
    log.info("Changing status of Installment (id={}, iud={}) from {} to {}", installment.getInstallmentId(), installment.getIud(), installment.getStatus(), status);
    InstallmentStatus statusFrom = getStatusFrom(installment);
    if (InstallmentUtils.isTransitionToSync(statusFrom, status)) {
      updateSyncStatus(installment, statusFrom, status);
    } else {
      installment.setStatus(status);
      installment.setSyncStatus(null);
    }

  }

  private static boolean isTransitionToSync(InstallmentStatus statusFrom,
    InstallmentStatus statusTo) {
    return TRANSITION_TO_SYNC_REQUIRED.contains(statusFrom + "|" + statusTo);
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
