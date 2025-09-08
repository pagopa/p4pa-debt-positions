package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceFetchService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
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
  private final OrganizationService organizationService;
  private final BalanceService balanceService;
  private final BalanceFetchService balanceFetchService;

  InstallmentUpdateService(DebtPositionRepository debtPositionRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, OrganizationService organizationService, BalanceService balanceService, BalanceFetchService balanceFetchService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.organizationService = organizationService;
    this.balanceService = balanceService;
    this.balanceFetchService = balanceFetchService;
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
        updateBalanceResolvingAmount(paidInstallment, debtPosition.getOrganizationId(), accessToken);
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

  private void updateInstallmentStatusAndFeeOfDebtPosition(InstallmentNoPII installment, InstallmentStatus status, ReceiptDTO receipt) {
    if (receipt != null) {
      installment.setReceiptId(receipt.getReceiptId());
      installment.setIur(receipt.getPaymentReceiptId());
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

  private void updateBalanceResolvingAmount(InstallmentNoPII installment, Long organizationId, String accessToken) {
    if (StringUtils.isBlank(installment.getBalance())) {
      DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId());
      if(debtPositionTypeOrg == null) {
        throw new NotFoundException("The DebtPositionTypeOrg for installment with id " + installment.getInstallmentId() + " was not found");
      }
      String balance = balanceFetchService.getBalanceDefault(organizationId, debtPositionTypeOrg, accessToken);
      installment.setBalance(balance);
    }

    if (StringUtils.isNotBlank(installment.getBalance())) {
      Organization org = organizationService.getOrganizationById(organizationId, accessToken)
        .orElseThrow(() -> new InvalidValueException("Provided organization id not found on db"));

      Long totalAmountCentsPrimaryOrg = installment.getTransfers().stream()
        .filter(transfer -> transfer.getOrgFiscalCode().equals(org.getOrgFiscalCode()))
        .mapToLong(Transfer::getAmountCents).sum();

      CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
        .balance(installment.getBalance())
        .amountCents(totalAmountCentsPrimaryOrg)
        .remittanceInformation(installment.getRemittanceInformation())
        .build();

      String balanceResolved = balanceService.calculateAmountBalance(amountBalanceRequest, accessToken);
      installment.setBalance(balanceResolved);
    }
  }
}
