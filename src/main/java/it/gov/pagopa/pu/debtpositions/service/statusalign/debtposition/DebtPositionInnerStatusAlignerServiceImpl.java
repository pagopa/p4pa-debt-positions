package it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;

public class DebtPositionInnerStatusAlignerServiceImpl extends DebtPositionStatusChecker implements DebtPositionInnerStatusAlignerService{

  private final DebtPositionRepository debtPositionRepository;

  public DebtPositionInnerStatusAlignerServiceImpl(DebtPositionRepository debtPositionRepository) {
    this.debtPositionRepository = debtPositionRepository;
  }

  @Override
  public void updateDebtPositionStatus(DebtPosition debtPosition) {
    updateEntityStatus(
      debtPosition,
      dp -> dp.getPaymentOptions().stream().map(PaymentOption::getStatus).toList(),
      this::determineDebtPositionStatus,
      DebtPosition::setStatus,
      (dp, newStatus) -> debtPositionRepository.updateStatus(dp.getDebtPositionId(), newStatus)
    );
  }
}
