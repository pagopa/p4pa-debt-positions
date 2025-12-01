package it.gov.pagopa.pu.debtpositions.util;

import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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

  public static final Set<PaymentOptionStatus> MODIFIABLE_PO_STATUSES = Set.of(
    PaymentOptionStatus.DRAFT,
    PaymentOptionStatus.UNPAID,
    PaymentOptionStatus.EXPIRED,
    PaymentOptionStatus.PARTIALLY_PAID,
    PaymentOptionStatus.UNPAYABLE);

  public static final Set<PaymentOptionStatus> PAYABLE_PO_STATUSES = Set.of(
    PaymentOptionStatus.UNPAID,
    PaymentOptionStatus.PARTIALLY_PAID);

  public static final Set<PaymentOptionStatus> PAYABLE_AND_EXPIRED_PO_STATUSES = Stream.concat(
    PAYABLE_PO_STATUSES.stream(),
    Stream.of(PaymentOptionStatus.EXPIRED)
  ).collect(Collectors.toSet());

  public static final Set<DebtPositionStatus> MODIFIABLE_DP_STATUSES = Set.of(
    DebtPositionStatus.DRAFT,
    DebtPositionStatus.UNPAID,
    DebtPositionStatus.EXPIRED,
    DebtPositionStatus.PARTIALLY_PAID);

  public static final Set<DebtPositionStatus> OPEN_DP_STATUSES = Set.of(
    DebtPositionStatus.UNPAID,
    DebtPositionStatus.PARTIALLY_PAID);

  public static final Set<DebtPositionStatus> DELETABLE_DP_STATUSES = Set.of(
    DebtPositionStatus.UNPAID,
    DebtPositionStatus.EXPIRED);

  public static final Set<InstallmentStatus> MODIFIABLE_STATUSES = Set.of(
    InstallmentStatus.DRAFT,
    InstallmentStatus.UNPAID,
    InstallmentStatus.EXPIRED,
    InstallmentStatus.UNPAYABLE);

  private static final Set<InstallmentStatus> PAYABLE_STATUSES = Set.of(
    InstallmentStatus.DRAFT,
    InstallmentStatus.TO_SYNC,
    InstallmentStatus.UNPAID,
    InstallmentStatus.EXPIRED);

  public static final Set<InstallmentStatus> PAID_STATUSES = Set.of(
    InstallmentStatus.PAID,
    InstallmentStatus.REPORTED);

  public static final Set<InstallmentStatus> PAYABLE_AND_EXPIRED_INSTALLMENT_STATUSES = Set.of(
    InstallmentStatus.UNPAID,
    InstallmentStatus.EXPIRED);

  private static final Set<InstallmentStatus> INVALIDABLE_STATUSES = Stream.concat(
    PAYABLE_STATUSES.stream(),
    Stream.of(InstallmentStatus.UNPAYABLE)
  ).collect(Collectors.toSet());

  public static final List<DebtPositionOrigin> ORDINARY_DEBT_POSITION_ORIGINS = List.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS,
    DebtPositionOrigin.SPONTANEOUS_SIL
  );

  public static final List<DebtPositionOrigin> PRIMARY_ORG_DEBT_POSITION_ORIGINS_NO_MIXED = Stream.concat(
    ORDINARY_DEBT_POSITION_ORIGINS.stream(),
    Stream.of(
      DebtPositionOrigin.RECEIPT_FILE,
      DebtPositionOrigin.RECEIPT_PAGOPA,
      DebtPositionOrigin.REPORTING_PAGOPA)
  ).toList();

  public static final List<DebtPositionOrigin> PRIMARY_ORG_DEBT_POSITION_ORIGINS = Stream.concat(
    PRIMARY_ORG_DEBT_POSITION_ORIGINS_NO_MIXED.stream(),
    Stream.of(
      DebtPositionOrigin.SPONTANEOUS_MIXED)
  ).toList();

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
