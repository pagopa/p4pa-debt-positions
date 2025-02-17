package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class InstallmentUpdateService {

  public static final Set<InstallmentStatus> NOT_PAID = Set.of(InstallmentStatus.DRAFT, InstallmentStatus.TO_SYNC, InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED);


  private final DebtPositionRepository debtPositionRepository;

  InstallmentUpdateService(DebtPositionRepository debtPositionRepository) {
    this.debtPositionRepository = debtPositionRepository;
  }

  public DebtPosition updateInstallmentStatusOfDebtPosition(InstallmentNoPII installment, ReceiptDTO receiptDTO) {
    //retrieve debt position
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.getInstallmentId());
    if(debtPosition == null) {
      throw new NotFoundException("debt position not found for installment " + installment.getInstallmentId());
    }

    //update installment status
    boolean[] primaryInstallmentFound = {false};
    debtPosition.getPaymentOptions().forEach(paymentOption -> {
      if (paymentOption.getPaymentOptionId().equals(installment.getPaymentOptionId())) {
        // current payment option: find this installment
        paymentOption.getInstallments().stream()
          .filter(anInstallment -> anInstallment.getInstallmentId().equals(installment.getInstallmentId()))
          .findAny().ifPresent(primaryInstallment -> {
            // set status of the found installment to PAID and link it to the receipt
            log.info("Installment [{}] found for receipt [{}]", primaryInstallment.getInstallmentId(), receiptDTO.getReceiptId());
            primaryInstallmentFound[0] = true;
            updateInstallmentFields(primaryInstallment, InstallmentStatus.PAID, receiptDTO.getReceiptId());
          });
      } else {
        // change status of every other installment of different payment options of this debt position to INVALID
        paymentOption.getInstallments().forEach(anInstallment -> {
          if (NOT_PAID.contains(anInstallment.getStatus())) {
            updateInstallmentFields(anInstallment, InstallmentStatus.INVALID, null);
          }
        });
      }
    });
    if(!primaryInstallmentFound[0]) {
      throw new NotFoundException("primary installment not found " + installment.getInstallmentId() + " on debt position " + debtPosition.getDebtPositionId());
    }

    return debtPosition;
  }

  private void updateInstallmentFields(InstallmentNoPII installment, InstallmentStatus status, Long receiptId) {
    if (receiptId != null) {
      installment.setReceiptId(receiptId);
    }
    updateSyncStatus(installment, status);
  }

  private void updateSyncStatus(InstallmentNoPII installment, InstallmentStatus to) {
    InstallmentStatus from = installment.getStatus().equals(InstallmentStatus.TO_SYNC) ? installment.getSyncStatus().getSyncStatusFrom() : installment.getStatus();
    installment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(from)
      .syncStatusTo(to)
      .build()
    );
    installment.setStatus(InstallmentStatus.TO_SYNC);
  }
}
