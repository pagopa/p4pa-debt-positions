package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionProcessorServiceImpl implements DebtPositionProcessorService {

  @Override
  public DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().forEach(paymentOption -> {
      long totalPaymentOptionAmount = paymentOption.getInstallments().stream()
        .filter(installment -> installment.getStatus() != InstallmentStatus.CANCELLED)
        .mapToLong(installment -> {
          long totalInstallmentAmount = installment.getTransfers().stream()
            .mapToLong(TransferDTO::getAmountCents)
            .sum();
          installment.setAmountCents(totalInstallmentAmount);
          return totalInstallmentAmount;
        })
        .sum();

      paymentOption.setTotalAmountCents(totalPaymentOptionAmount);
    });

    return debtPositionDTO;
  }

}
