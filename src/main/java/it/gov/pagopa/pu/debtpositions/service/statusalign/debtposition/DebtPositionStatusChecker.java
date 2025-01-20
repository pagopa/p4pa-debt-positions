package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.service.statusalign.StatusRulesHandler;
import org.springframework.stereotype.Service;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus.*;

@Service
public class DebtPositionStatusChecker extends StatusRulesHandler<PaymentOptionStatus> {

  public DebtPositionStatusChecker() {
    super(TO_SYNC, PAID, UNPAID, EXPIRED, CANCELLED, REPORTED, INVALID);
  }

  public DebtPositionStatus determineDebtPositionStatus(List<PaymentOptionStatus> paymentOptionStatusList) {
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
}
