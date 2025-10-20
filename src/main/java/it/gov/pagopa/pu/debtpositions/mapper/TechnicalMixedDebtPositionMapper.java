package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class TechnicalMixedDebtPositionMapper {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final BalanceResolverService balanceResolverService;

  public TechnicalMixedDebtPositionMapper(BalanceResolverService balanceResolverService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    this.balanceResolverService = balanceResolverService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  public DebtPosition toTechnicalMixedDebtPosition(DebtPosition debtPosition,
                                                   Long debtPositionTypeOrgId,
                                                   boolean isUnpayable,
                                                   List<MixedDpAdditionalData> mixedDpAdditionalDataList,
                                                   String accessToken) {
    DebtPosition technicalMixedDp = new DebtPosition();
    technicalMixedDp.setIupdOrg(debtPosition.getIupdOrg());
    technicalMixedDp.setDescription(debtPosition.getDescription());
    technicalMixedDp.setStatus(
      isUnpayable ? DebtPositionStatus.UNPAID : DebtPositionStatus.PAID);
    technicalMixedDp.setDebtPositionOrigin(
      DebtPositionOrigin.SPONTANEOUS_MIXED);
    technicalMixedDp.setOrganizationId(debtPosition.getOrganizationId());
    technicalMixedDp.setDebtPositionTypeOrgId(debtPositionTypeOrgId);
    technicalMixedDp.setValidityDate(debtPosition.getValidityDate());
    technicalMixedDp.setFlagIuvVolatile(debtPosition.isFlagIuvVolatile());
    technicalMixedDp.setMultiDebtor(debtPosition.isMultiDebtor());
    technicalMixedDp.setFlagPuPagoPaPayment(
      debtPosition.isFlagPuPagoPaPayment());
    technicalMixedDp.setPaymentOptions(
      toTechnicalMixedDPPaymentOptions(
        debtPosition, isUnpayable, mixedDpAdditionalDataList, debtPositionTypeOrgId, accessToken));

    return technicalMixedDp;
  }

  private SortedSet<PaymentOption> toTechnicalMixedDPPaymentOptions(
    DebtPosition debtPosition, boolean isUnpayable,
    List<MixedDpAdditionalData> mixedDpAdditionalDataList,
    Long debtPositionTypeOrgId, String accessToken) {
    SortedSet<PaymentOption> set = new TreeSet<>();

    for (PaymentOption paymentOption : debtPosition.getPaymentOptions()) {
      PaymentOption technicalMixedDpPaymentOption = new PaymentOption();
      technicalMixedDpPaymentOption.setTotalAmountCents(
        paymentOption.getTotalAmountCents());
      technicalMixedDpPaymentOption.setStatus(
        isUnpayable ? PaymentOptionStatus.UNPAYABLE : PaymentOptionStatus.PAID);
      technicalMixedDpPaymentOption.setDescription(
        paymentOption.getDescription());
      technicalMixedDpPaymentOption.setPaymentOptionType(
        paymentOption.getPaymentOptionType());
      technicalMixedDpPaymentOption.setPaymentOptionIndex(
        paymentOption.getPaymentOptionIndex());
      technicalMixedDpPaymentOption.setInstallments(
        toTechnicalMixedDPInstallments(debtPosition.getOrganizationId(),
          paymentOption.getInstallments(),
          isUnpayable, mixedDpAdditionalDataList, debtPositionTypeOrgId, accessToken));

      set.add(technicalMixedDpPaymentOption);
    }

    return set;
  }

  private SortedSet<InstallmentNoPII> toTechnicalMixedDPInstallments(
    Long organizationId, SortedSet<InstallmentNoPII> installments, boolean isUnpayable,
    List<MixedDpAdditionalData> mixedDpAdditionalDataList,
    Long debtPositionTypeOrgId, String accessToken) {
    SortedSet<InstallmentNoPII> set = new TreeSet<>();

    installments.forEach(installment ->
      mixedDpAdditionalDataList.forEach(mixedDpAdditionalData -> {
        Transfer transfer = installment.getTransfers().stream()
          .filter(t -> mixedDpAdditionalData.getTransferIndex()
            .equals(t.getTransferIndex())).findFirst()
          .orElseThrow(() -> new IllegalStateException(
            "There is no Transfer having transferIndex: [%s] associated with Installment having id: [%d].".formatted(
              mixedDpAdditionalData.getTransferIndex(),
              installment.getInstallmentId())));

        InstallmentNoPII technicalMixedDpInstallment = new InstallmentNoPII();
        technicalMixedDpInstallment.setStatus(
          isUnpayable ? InstallmentStatus.UNPAYABLE : InstallmentStatus.PAID);
        technicalMixedDpInstallment.setSyncStatus(null);
        technicalMixedDpInstallment.setIupdPagopa(installment.getIupdPagopa());
        technicalMixedDpInstallment.setGenerateNotice(
          installment.isGenerateNotice());
        technicalMixedDpInstallment.setIud(mixedDpAdditionalData.getIud());
        technicalMixedDpInstallment.setIuv(installment.getIuv());
        technicalMixedDpInstallment.setIur(installment.getIur());
        technicalMixedDpInstallment.setIuf(installment.getIuf());
        technicalMixedDpInstallment.setNav(installment.getNav());
        technicalMixedDpInstallment.setIun(installment.getIun());
        technicalMixedDpInstallment.setDueDate(installment.getDueDate());
        technicalMixedDpInstallment.setSwitchToExpired(
          installment.isSwitchToExpired());
        technicalMixedDpInstallment.setNotificationFeeCents(
          installment.getNotificationFeeCents());
        technicalMixedDpInstallment.setAmountCents(
          transfer.getAmountCents());
        technicalMixedDpInstallment.setRemittanceInformation(
          installment.getRemittanceInformation());
        technicalMixedDpInstallment.setBalance(
          mixedDpAdditionalData.getBalance());
        technicalMixedDpInstallment.setLegacyPaymentMetadata(
          mixedDpAdditionalData.getLegacyPaymentMetadata());
        technicalMixedDpInstallment.setPersonalDataId(
          installment.getPersonalDataId());
        technicalMixedDpInstallment.setDebtorEntityType(
          installment.getDebtorEntityType());
        technicalMixedDpInstallment.setDebtorFiscalCodeHash(
          installment.getDebtorFiscalCodeHash());
        technicalMixedDpInstallment.setNotificationDate(
          installment.getNotificationDate());
        technicalMixedDpInstallment.setIngestionFlowFileId(
          installment.getIngestionFlowFileId());
        technicalMixedDpInstallment.setIngestionFlowFileLineNumber(
          installment.getIngestionFlowFileLineNumber());
        technicalMixedDpInstallment.setIngestionFlowFileAction(
          installment.getIngestionFlowFileAction());
        technicalMixedDpInstallment.setSourceFlowName(
          installment.getSourceFlowName());
        technicalMixedDpInstallment.setReceiptId(installment.getReceiptId());

        technicalMixedDpInstallment.setTransfers(
          toTechnicalMixedDPTransfers(transfer));

        if (!isUnpayable) {
          DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
            .orElseThrow(() -> new NotFoundException("The DebtPositionTypeOrg with id " + debtPositionTypeOrgId + " was not found"));
          balanceResolverService.updateBalanceResolvingAmount(technicalMixedDpInstallment, organizationId, debtPositionTypeOrg, accessToken);
        }
        set.add(technicalMixedDpInstallment);
      }));

    return set;
  }

  private SortedSet<Transfer> toTechnicalMixedDPTransfers(Transfer transfer) {
    Transfer technicalMixedDpTransfer = new Transfer();
    technicalMixedDpTransfer.setTransferIndex(transfer.getTransferIndex());
    technicalMixedDpTransfer.setOrgFiscalCode(transfer.getOrgFiscalCode());
    technicalMixedDpTransfer.setOrgName(transfer.getOrgName());
    technicalMixedDpTransfer.setAmountCents(transfer.getAmountCents());
    technicalMixedDpTransfer.setRemittanceInformation(
      transfer.getRemittanceInformation());
    technicalMixedDpTransfer.setStamp(transfer.getStamp());
    technicalMixedDpTransfer.setIban(transfer.getIban());
    technicalMixedDpTransfer.setPostalIban(transfer.getPostalIban());
    technicalMixedDpTransfer.setCategory(transfer.getCategory());
    technicalMixedDpTransfer.setMbdAttachment(transfer.getMbdAttachment());

    SortedSet<Transfer> set = new TreeSet<>();
    set.add(technicalMixedDpTransfer);
    return set;
  }
}
