package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.pii.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DebtPositionDeleteService {

  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentPIIRepository;
  private final TransferRepository transferRepository;

  public DebtPositionDeleteService(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository, InstallmentPIIRepository installmentPIIRepository, TransferRepository transferRepository) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentPIIRepository = installmentPIIRepository;
    this.transferRepository = transferRepository;
  }

  @Transactional
  public void delete(DebtPosition debtPosition) {
    debtPosition.getPaymentOptions()
      .forEach(paymentOption -> {
        paymentOption.getInstallments()
          .forEach(installment -> {
            installment.getTransfers().forEach(transferRepository::delete);
            installmentPIIRepository.delete(installment);
          });
        paymentOptionRepository.delete(paymentOption);
      });
    debtPositionRepository.delete(debtPosition);
  }
}
