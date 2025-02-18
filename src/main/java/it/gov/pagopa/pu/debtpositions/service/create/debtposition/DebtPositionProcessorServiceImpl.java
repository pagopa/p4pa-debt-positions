package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionProcessorServiceImpl implements DebtPositionProcessorService {

  @Override
  public DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().forEach(this::updatePaymentOptionAmounts);
    return debtPositionDTO;
  }

  @Override
  public PaymentOptionDTO updatePaymentOptionAmounts(PaymentOptionDTO paymentOptionDTO) {
      long totalPaymentOptionAmount = paymentOptionDTO.getInstallments().stream()
        .filter(installment -> installment.getStatus() != InstallmentStatus.CANCELLED)
        .mapToLong(installment -> {
          long totalInstallmentAmount = installment.getTransfers().stream()
            .mapToLong(TransferDTO::getAmountCents)
            .sum();
          installment.setAmountCents(totalInstallmentAmount);
          return totalInstallmentAmount;
        })
        .sum();

    paymentOptionDTO.setTotalAmountCents(totalPaymentOptionAmount);

    return paymentOptionDTO;
  }
}
