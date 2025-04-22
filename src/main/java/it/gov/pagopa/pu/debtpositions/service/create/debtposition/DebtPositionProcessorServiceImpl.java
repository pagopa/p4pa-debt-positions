package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionProcessorServiceImpl implements DebtPositionProcessorService {

  @Override
  public void updateAmounts(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      long totalPaymentOptionAmount = paymentOption.getInstallments().stream()
        .filter(this::isInstallmentCancelled)
        .mapToLong(InstallmentDTO::getAmountCents)
        .sum();

      paymentOption.setTotalAmountCents(totalPaymentOptionAmount);
    });
  }

  private boolean isInstallmentCancelled(InstallmentDTO installment){
    return !(installment.getStatus() == InstallmentStatus.CANCELLED ||
      (installment.getSyncStatus() != null && InstallmentStatus.CANCELLED.equals(installment.getSyncStatus().getSyncStatusTo())));
  }
}
