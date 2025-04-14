package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class InstallmentUtils {

  private InstallmentUtils() {
  }

  private static final Set<String> TRANSITION_TO_SYNC_REQUIRED = Set.of(
    InstallmentStatus.DRAFT + "|" + InstallmentStatus.UNPAID,
    InstallmentStatus.UNPAYABLE + "|" + InstallmentStatus.UNPAID,
    InstallmentStatus.EXPIRED + "|" + InstallmentStatus.UNPAID,
    InstallmentStatus.UNPAID + "|" + InstallmentStatus.UNPAID,
    InstallmentStatus.UNPAID + "|" + InstallmentStatus.INVALID,
    InstallmentStatus.UNPAID + "|" + InstallmentStatus.CANCELLED);

  private static final Set<InstallmentStatus> PAYABLE_STATUSES = Set.of(
    InstallmentStatus.DRAFT,
    InstallmentStatus.TO_SYNC,
    InstallmentStatus.UNPAID,
    InstallmentStatus.EXPIRED);

  private static final Set<InstallmentStatus> INVALIDABLE_STATUSES = Stream.concat(
    PAYABLE_STATUSES.stream(),
    Stream.of(InstallmentStatus.UNPAYABLE)
  ).collect(Collectors.toSet());

  /**
   * It will check if the Installment is in a payable status
   */
  public static boolean isInvalidable(BaseInstallment installment) {
    return INVALIDABLE_STATUSES.contains(installment.getStatus());
  }

  /**
   * It will check if the Installment is in a payable status
   */
  public static boolean isPayable(BaseInstallment installment) {
    return PAYABLE_STATUSES.contains(installment.getStatus());
  }

  /**
   * It will set the Installment status verifying it will need the TO_SYNC transition
   *
   * @param installment the installment to update
   * @param status      the new status
   */
  public static void setStatus(BaseInstallment installment, InstallmentStatus status) {
    log.info("Changing status of Installment (id={}, iud={}) from {} to {}", installment.getInstallmentId(), installment.getIud(), installment.getStatus(), status);
    InstallmentStatus statusFrom = getStatusFrom(installment);
    if (InstallmentUtils.isTransitionToSync(statusFrom, status)) {
      updateSyncStatus(installment, status);
    } else {
      installment.setStatus(status);
      installment.setSyncStatus(null);
    }
  }

  private static boolean isTransitionToSync(InstallmentStatus statusFrom, InstallmentStatus statusTo) {
    return TRANSITION_TO_SYNC_REQUIRED.contains(statusFrom + "|" + statusTo);
  }

  private static void updateSyncStatus(BaseInstallment installment, InstallmentStatus to) {
    InstallmentStatus from = getStatusFrom(installment);
    installment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(from)
      .syncStatusTo(to)
      .build()
    );
    installment.setStatus(InstallmentStatus.TO_SYNC);
  }

  private static InstallmentStatus getStatusFrom(BaseInstallment installment) {
    if (installment.getStatus().equals(InstallmentStatus.TO_SYNC)) {
      log.warn("Transitioning Installment (installmentId={}, iud={}) from TO_SYNC status! {}",
        installment.getInstallmentId(), installment.getIud(), installment.getSyncStatus());
      return installment.getSyncStatus().getSyncStatusFrom();
    } else {
      return installment.getStatus();
    }
  }
}
