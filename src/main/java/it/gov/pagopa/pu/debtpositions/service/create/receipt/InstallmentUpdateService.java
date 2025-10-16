package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Service
@Slf4j
public class InstallmentUpdateService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final BalanceResolverService balanceResolverService;

  InstallmentUpdateService(DebtPositionRepository debtPositionRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, BalanceResolverService balanceResolverService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.balanceResolverService = balanceResolverService;
  }

  @Transactional
  public DebtPosition updateInstallmentStatusOfDebtPosition(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    //retrieve debt position
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByInstallmentId(installment.getInstallmentId());
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
        updateInstallmentStatusAndFeeOfDebtPosition(paidInstallment, InstallmentStatus.PAID, receiptDTO);
        DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId());
        if (debtPositionTypeOrg == null) {
          throw new NotFoundException("The DebtPositionTypeOrg for installment with id " + installment.getInstallmentId() + " was not found");
        }
        balanceResolverService.updateBalanceResolvingAmount(paidInstallment, debtPosition.getOrganizationId(), debtPositionTypeOrg, accessToken);
        // update mbdAttachment of transfer entity if present in input ReceiptDTO
        updateMbdAttachment(receiptDTO, paidInstallment);
      }, () -> {
        throw new NotFoundException("primary installment not found " + installment.getInstallmentId() + " on debt position " + debtPosition.getDebtPositionId());
      });

    return debtPosition;
  }

  private static void updateMbdAttachment(ReceiptWithAdditionalNodeDataDTO receiptDTO,
                                          InstallmentNoPII paidInstallment) {
    receiptDTO.getTransfers().stream()
      .filter(receiptTransferDTO -> receiptTransferDTO.getMbdAttachment() != null)
      .findFirst()
      .flatMap(receiptTransferDTO -> paidInstallment.getTransfers().stream()
        .filter(transfer -> receiptTransferDTO.getIdTransfer().equals(transfer.getTransferIndex()))
        .findFirst()).ifPresent(transfer -> transfer.setMbdAttachment(transfer.getMbdAttachment()));
  }


  private void invalidOtherPaymentOptions(PaymentOption paymentOption) {
    paymentOption.getInstallments().forEach(anInstallment -> {
      if (InstallmentUtils.isInvalidable(anInstallment)) {
        updateInstallmentStatusAndFeeOfDebtPosition(anInstallment, InstallmentStatus.INVALID, null);
      }
    });
  }

  private void updateInstallmentStatusAndFeeOfDebtPosition(InstallmentNoPII installment, InstallmentStatus status, ReceiptWithAdditionalNodeDataDTO receipt) {
    if (receipt != null) {
      installment.setReceiptId(receipt.getReceiptId());
      installment.setIur(receipt.getPaymentReceiptId());
      if (StringUtils.isNotBlank(receipt.getBalance())) {
        installment.setBalance(receipt.getBalance());
      }
      long feeAmountCents = receipt.getPaymentAmountCents() - installment.getAmountCents();
      if (feeAmountCents > 0) {
        log.debug("Set NotificationFeeCents for installmentId {} with amount: {}", installment.getInstallmentId(), feeAmountCents);
        installment.setNotificationFeeCents(feeAmountCents);
        log.debug("Update amounts");
        installment.setAmountCents(installment.getAmountCents() + feeAmountCents);
        installment.getTransfers()
          .stream().filter(t -> t.getTransferIndex() == 1)
          .findFirst()
          .ifPresent(transfer -> transfer.setAmountCents(transfer.getAmountCents() + feeAmountCents));
      }
    }
    InstallmentUtils.setStatus(installment, status);
  }
}
