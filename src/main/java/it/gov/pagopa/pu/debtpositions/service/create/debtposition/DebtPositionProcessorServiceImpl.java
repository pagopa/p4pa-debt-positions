package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.BaseInstallment;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionProcessorServiceImpl implements DebtPositionProcessorService {

  @Override
  public void updateAmounts(BaseDebtPosition debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      long totalPaymentOptionAmount = paymentOption.getInstallments().stream()
        .filter(this::isInstallmentCancelled)
        .mapToLong(BaseInstallment::getAmountCents)
        .sum();

      paymentOption.setTotalAmountCents(totalPaymentOptionAmount);
    });
  }

  private boolean isInstallmentCancelled(BaseInstallment installment){
    return !(installment.getStatus() == InstallmentStatus.CANCELLED ||
      (installment.getSyncStatus() != null && InstallmentStatus.CANCELLED.equals(installment.getSyncStatus().getSyncStatusTo())));
  }
}
