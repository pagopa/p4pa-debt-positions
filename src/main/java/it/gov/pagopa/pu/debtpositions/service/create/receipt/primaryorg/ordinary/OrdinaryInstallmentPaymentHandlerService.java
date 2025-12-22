package it.gov.pagopa.pu.debtpositions.service.create.receipt.primaryorg.ordinary;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrdinaryInstallmentPaymentHandlerService {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final BalanceResolverService balanceResolverService;

  public OrdinaryInstallmentPaymentHandlerService(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, BalanceResolverService balanceResolverService) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.balanceResolverService = balanceResolverService;
  }

  public void updateInstallment(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO, String accessToken) {
    log.info("Setting Installment [{}] as paid through receipt [{}]",
      installment.getInstallmentId(), receiptDTO.getReceiptId());

    InstallmentStatus previousStatus = installment.getStatus();
    updateInstallmentFromReceipt(installment, receiptDTO);

    if (!InstallmentUtils.PAID_STATUSES.contains(previousStatus)) {
      if (StringUtils.isNotBlank(receiptDTO.getBalance())) {
        installment.setBalance(receiptDTO.getBalance());
      }
      resolveBalance(installment, accessToken);
      updateMbdAttachment(installment, receiptDTO);
    } else if (ReceiptOriginType.RECEIPT_FILE.equals(receiptDTO.getReceiptOrigin())) {
      if (StringUtils.isNotBlank(receiptDTO.getBalance())) {
        installment.setBalance(receiptDTO.getBalance());
        resolveBalance(installment, accessToken);
      }
      updateMbdAttachment(installment, receiptDTO);
    }
  }

  private void updateInstallmentFromReceipt(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    installment.setReceiptId(receiptDTO.getReceiptId());
    InstallmentUtils.setStatus(installment, InstallmentStatus.PAID);
    installment.setIur(receiptDTO.getPaymentReceiptId());
    updateAmountAndNotificationFee(installment, receiptDTO);
  }

  private void updateAmountAndNotificationFee(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    long feeAmountCents = receiptDTO.getPaymentAmountCents() - installment.getAmountCents();
    if (feeAmountCents > 0) {
      log.debug("Set NotificationFeeCents for installmentId {} with amount: {}", installment.getInstallmentId(), feeAmountCents);
      long notificationFeeCents = ReceiptOriginType.RECEIPT_PAGOPA.equals(receiptDTO.getReceiptOrigin())
        ? Long.parseLong(receiptDTO.getMetadata().get("NOTIFICATION_FEE"))
        : feeAmountCents;

      installment.setNotificationFeeCents(notificationFeeCents);

      installment.setAmountCents(installment.getAmountCents() + feeAmountCents);
      installment.getTransfers()
        .stream().filter(t -> t.getTransferIndex() == 1)
        .findFirst()
        .ifPresent(transfer -> transfer.setAmountCents(transfer.getAmountCents() + feeAmountCents));
    }
  }

  public void resolveBalance(InstallmentNoPII installment, String accessToken) {
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.getDebtPositionTypeOrgByInstallmentId(installment.getInstallmentId());
    if (debtPositionTypeOrg == null) {
      throw new NotFoundException("The DebtPositionTypeOrg for installment with id " + installment.getInstallmentId() + " was not found");
    }
    resolveBalance(installment, debtPositionTypeOrg, accessToken);
  }

  public void resolveBalance(InstallmentNoPII installment, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    balanceResolverService.updateBalanceResolvingAmount(installment, debtPositionTypeOrg.getOrganizationId(), debtPositionTypeOrg, accessToken);
  }

  private void updateMbdAttachment(InstallmentNoPII installment, ReceiptWithAdditionalNodeDataDTO receiptDTO) {
    receiptDTO.getTransfers().stream()
      .filter(receiptTransferDTO -> receiptTransferDTO.getMbdAttachment() != null)
      .forEach(receiptTransferDTO ->
        installment.getTransfers().stream()
          .filter(transfer -> receiptTransferDTO.getIdTransfer().equals(transfer.getTransferIndex()))
          .forEach(transfer -> transfer.setMbdAttachment(receiptTransferDTO.getMbdAttachment()))
      );
  }
}
