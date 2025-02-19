package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionProcessorServiceImpl implements DebtPositionProcessorService {

  private final PaymentOptionRepository paymentOptionRepository;

  public DebtPositionProcessorServiceImpl(PaymentOptionRepository paymentOptionRepository) {
    this.paymentOptionRepository = paymentOptionRepository;
  }

  @Override
  public DebtPositionDTO updateAmounts(DebtPositionDTO debtPositionDTO) {
    debtPositionDTO.getPaymentOptions().forEach(this::updatePaymentOptionAmounts);
    return debtPositionDTO;
  }

  @Override
  public void updatePaymentOptionAmounts(PaymentOptionDTO paymentOptionDTO) {
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
  }

  @Override
  public void synchronizeAmountsAndStatus(DebtPositionDTO debtPositionDTO, InstallmentDTO installmentDTO) {
    if (InstallmentStatus.TO_SYNC.equals(installmentDTO.getStatus())) {
      debtPositionDTO.getPaymentOptions().stream()
        .filter(paymentOptionDTO -> paymentOptionDTO.getPaymentOptionId().equals(installmentDTO.getPaymentOptionId()))
        .findFirst()
        .ifPresent(paymentOptionDTO -> {
          updatePaymentOptionAmounts(paymentOptionDTO);
          paymentOptionDTO.setStatus(PaymentOptionStatus.TO_SYNC);
          paymentOptionRepository.updateStatusAndTotalAmounts(paymentOptionDTO.getPaymentOptionId(),
            paymentOptionDTO.getStatus(), paymentOptionDTO.getTotalAmountCents());
        });

      debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    }
  }
}
