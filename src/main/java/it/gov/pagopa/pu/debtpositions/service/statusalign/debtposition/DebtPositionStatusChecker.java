package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.BasePaymentOption;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.StatusRulesHandler;

import java.util.List;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus.*;

public class DebtPositionStatusChecker extends StatusRulesHandler<PaymentOptionStatus, BaseDebtPosition, DebtPositionStatus> {

  private final DebtPositionRepository debtPositionRepository;

  public DebtPositionStatusChecker(DebtPositionRepository debtPositionRepository) {
    super(TO_SYNC, PAID, DRAFT, UNPAID, UNPAYABLE, EXPIRED, CANCELLED, INVALID, REPORTED);
    this.debtPositionRepository = debtPositionRepository;
  }

  @Override
  public DebtPositionStatus calculateNewStatus(List<PaymentOptionStatus> paymentOptionStatusList) {
    if (isToSync(paymentOptionStatusList)){
      return DebtPositionStatus.TO_SYNC;
    } else if (isPartiallyPaid(paymentOptionStatusList)){
      return DebtPositionStatus.PARTIALLY_PAID;
    } else if (isDraft(paymentOptionStatusList)) {
      return DebtPositionStatus.DRAFT;
    } else if (isUnpaid(paymentOptionStatusList)){
      return DebtPositionStatus.UNPAID;
    } else if (isPaid(paymentOptionStatusList)){
      return DebtPositionStatus.PAID;
    } else if (isReported(paymentOptionStatusList)){
      return DebtPositionStatus.REPORTED;
    } else if (isCancelled(paymentOptionStatusList)){
      return DebtPositionStatus.CANCELLED;
    } else if (isExpired(paymentOptionStatusList)){
      return DebtPositionStatus.EXPIRED;
    } else {
      throw new InvalidValueException("Unable to determine status for DebtPosition having paymentOptionStatuses: " + paymentOptionStatusList);
    }
  }

  @Override
  public boolean isPartiallyPaid(List<PaymentOptionStatus> childrenStatusList) {
    return childrenStatusList.contains(PARTIALLY_PAID);
  }


  @Override
  protected List<PaymentOptionStatus> getChildStatuses(BaseDebtPosition debtPosition) {
    return debtPosition.getPaymentOptions().stream()
      .map(BasePaymentOption::getStatus)
      .toList();
  }

  @Override
  protected void setStatus(BaseDebtPosition debtPosition, DebtPositionStatus newStatus) {
    debtPosition.setStatus(newStatus);
  }

  @Override
  protected void storeStatus(BaseDebtPosition debtPosition, DebtPositionStatus newStatus) {
    debtPositionRepository.updateStatus(debtPosition.getDebtPositionId(), newStatus);
  }

  @Override
  protected Set<PaymentOptionStatus> getAllowedCancelledStatuses() {
    return Set.of(CANCELLED, INVALID, UNPAYABLE, EXPIRED);
  }
}
