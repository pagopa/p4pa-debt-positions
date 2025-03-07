package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstallmentUpdateService {

  private final DebtPositionRepository debtPositionRepository;

  InstallmentUpdateService(DebtPositionRepository debtPositionRepository) {
    this.debtPositionRepository = debtPositionRepository;
  }

  public DebtPosition updateInstallmentStatusOfDebtPosition(InstallmentNoPII installment, ReceiptDTO receiptDTO) {
    //retrieve debt position
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.getInstallmentId());
    if (debtPosition == null) {
      throw new NotFoundException("debt position not found for installment " + installment.getInstallmentId());
    }

    debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> {
        if (paymentOption.getPaymentOptionId().equals(installment.getPaymentOptionId())) {
          // current payment option: find this installment
          return paymentOption.getInstallments().stream();
        } else {
          // change status of every other installment of different payment options of this debt position to INVALID
          invalidOtherPaymentOptions(paymentOption);
          return Stream.empty();
        }
      })
      .filter(anInstallment -> anInstallment.getInstallmentId().equals(installment.getInstallmentId()))
      .toList().stream() // we need to force evaluation of the entire stream
      .findFirst() // we are sure that we may not have more than one element
      .ifPresentOrElse(paidInstallment -> {
        // set status of the found installment to PAID and link it to the receipt
        log.info("Installment [{}] found for receipt [{}]", paidInstallment.getInstallmentId(), receiptDTO.getReceiptId());
        updateInstallment(paidInstallment, InstallmentStatus.PAID, receiptDTO.getReceiptId());
      }, () -> {
        throw new NotFoundException("primary installment not found " + installment.getInstallmentId() + " on debt position " + debtPosition.getDebtPositionId());
      });

    return debtPosition;
  }



  private void invalidOtherPaymentOptions(PaymentOption paymentOption) {
    paymentOption.getInstallments().forEach(anInstallment -> {
      if (InstallmentUtils.isPayable(anInstallment)) {
        updateInstallment(anInstallment, InstallmentStatus.INVALID, null);
      }
    });
  }

  private void updateInstallment(InstallmentNoPII installment, InstallmentStatus status, Long receiptId){
    if (receiptId != null) {
      installment.setReceiptId(receiptId);
    }
    InstallmentUtils.setStatus(installment, status);

  }

}
