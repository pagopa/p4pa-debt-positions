package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;

import java.io.Serializable;
import java.util.Collection;

public interface BaseDebtPosition extends Serializable {
  Long getDebtPositionId();

  DebtPositionStatus getStatus();
  void setStatus(DebtPositionStatus status);

  Collection<? extends BasePaymentOption> getPaymentOptions();
}
