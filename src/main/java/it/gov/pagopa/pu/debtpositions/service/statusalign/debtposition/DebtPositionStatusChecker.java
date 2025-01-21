package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.StatusRulesHandler;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus.*;

public class DebtPositionStatusChecker extends StatusRulesHandler<PaymentOptionStatus, DebtPosition, DebtPositionStatus> {

  private final DebtPositionRepository debtPositionRepository;

  public DebtPositionStatusChecker(DebtPositionRepository debtPositionRepository) {
    super(TO_SYNC, PAID, UNPAID, EXPIRED, CANCELLED, REPORTED, INVALID);
    this.debtPositionRepository = debtPositionRepository;
  }

  @Override
  public DebtPositionStatus calculateNewStatus(List<PaymentOptionStatus> paymentOptionStatusList) {
    if (isToSync(paymentOptionStatusList)){
      return DebtPositionStatus.TO_SYNC;
    } else if (isPartiallyPaid(paymentOptionStatusList)){
      return DebtPositionStatus.PARTIALLY_PAID;
    } else if (isUnpaid(paymentOptionStatusList)){
      return DebtPositionStatus.UNPAID;
    } else if (isPaid(paymentOptionStatusList)){
      return DebtPositionStatus.PAID;
    } else if (isReported(paymentOptionStatusList)){
      return DebtPositionStatus.REPORTED;
    } else if (isInvalid(paymentOptionStatusList)){
      return DebtPositionStatus.INVALID;
    } else if (isCancelled(paymentOptionStatusList)){
      return DebtPositionStatus.CANCELLED;
    } else if (isExpired(paymentOptionStatusList)){
      return DebtPositionStatus.EXPIRED;
    } else {
      throw new InvalidStatusException("Unable to determine status for DebtPosition");
    }
  }

  @Override
  protected List<PaymentOptionStatus> getChildStatuses(DebtPosition debtPosition) {
    return debtPosition.getPaymentOptions().stream()
      .map(PaymentOption::getStatus)
      .toList();
  }

  @Override
  protected void setStatus(DebtPosition debtPosition, DebtPositionStatus newStatus) {
    debtPosition.setStatus(newStatus);
  }

  @Override
  protected void storeStatus(DebtPosition debtPosition, DebtPositionStatus newStatus) {
    debtPositionRepository.updateStatus(debtPosition.getDebtPositionId(), newStatus);
  }
}
