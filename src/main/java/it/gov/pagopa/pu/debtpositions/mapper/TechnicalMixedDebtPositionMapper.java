package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.exception.common.IllegalStateBusinessException;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.BalanceResolverService;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.Constants.UNKNOWN_STATION_ID;

@Service
public class TechnicalMixedDebtPositionMapper {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final BalanceResolverService balanceResolverService;
  private final DebtPositionProcessorService debtPositionProcessorService;

  public TechnicalMixedDebtPositionMapper(BalanceResolverService balanceResolverService, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, DebtPositionProcessorService debtPositionProcessorService) {
    this.balanceResolverService = balanceResolverService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
  }

  public DebtPosition toTechnicalMixedDebtPosition(DebtPosition debtPosition,
                                                   Long debtPositionTypeOrgId,
                                                   boolean isUnpaid,
                                                   List<MixedDpAdditionalData> mixedDpAdditionalDataList,
                                                   OffsetDateTime paymentDateTime,
                                                   String accessToken) {
    DebtPosition technicalMixedDp = new DebtPosition();
    technicalMixedDp.setIupdOrg(debtPosition.getIupdOrg());
    technicalMixedDp.setDescription(debtPosition.getDescription());
    technicalMixedDp.setStatus(
      isUnpaid ? DebtPositionStatus.UNPAID : DebtPositionStatus.PAID);
    technicalMixedDp.setDebtPositionOrigin(
      DebtPositionOrigin.SPONTANEOUS_MIXED);
    technicalMixedDp.setOrganizationId(debtPosition.getOrganizationId());
    technicalMixedDp.setDebtPositionTypeOrgId(debtPositionTypeOrgId);
    technicalMixedDp.setValidityDate(debtPosition.getValidityDate());
    technicalMixedDp.setMultiDebtor(debtPosition.isMultiDebtor());
    technicalMixedDp.setFlagPuPagoPaPayment(
      debtPosition.isFlagPuPagoPaPayment());
    technicalMixedDp.setPaymentOptions(
      toTechnicalMixedDPPaymentOptions(
        debtPosition, isUnpaid, mixedDpAdditionalDataList, debtPositionTypeOrgId, paymentDateTime, accessToken));
    debtPositionProcessorService.updateAmounts(technicalMixedDp);
    technicalMixedDp.setStationId(UNKNOWN_STATION_ID);

    return technicalMixedDp;
  }

  private SortedSet<PaymentOption> toTechnicalMixedDPPaymentOptions(
    DebtPosition debtPosition, boolean isUnpaid,
    List<MixedDpAdditionalData> mixedDpAdditionalDataList,
    Long debtPositionTypeOrgId, OffsetDateTime paymentDateTime, String accessToken) {
    SortedSet<PaymentOption> set = new TreeSet<>();

    for (PaymentOption paymentOption : debtPosition.getPaymentOptions()) {
      PaymentOption technicalMixedDpPaymentOption = new PaymentOption();
      technicalMixedDpPaymentOption.setTotalAmountCents(
        paymentOption.getTotalAmountCents());
      technicalMixedDpPaymentOption.setStatus(
        isUnpaid ? PaymentOptionStatus.UNPAID : PaymentOptionStatus.PAID);
      technicalMixedDpPaymentOption.setDescription(
        paymentOption.getDescription());
      technicalMixedDpPaymentOption.setPaymentOptionType(
        mixedDpAdditionalDataList.size() == 1 ? PaymentOptionType.SINGLE_INSTALLMENT : PaymentOptionType.INSTALLMENTS);
      technicalMixedDpPaymentOption.setPaymentOptionIndex(
        paymentOption.getPaymentOptionIndex());
      technicalMixedDpPaymentOption.setInstallments(
        toTechnicalMixedDPInstallments(debtPosition.getOrganizationId(),
          paymentOption.getInstallments(),
          isUnpaid, mixedDpAdditionalDataList, debtPositionTypeOrgId, paymentDateTime, accessToken));

      set.add(technicalMixedDpPaymentOption);
    }

    return set;
  }

  private SortedSet<InstallmentNoPII> toTechnicalMixedDPInstallments(
    Long organizationId, SortedSet<InstallmentNoPII> installments, boolean isUnpaid,
    List<MixedDpAdditionalData> mixedDpAdditionalDataList,
    Long debtPositionTypeOrgId, OffsetDateTime paymentDateTime, String accessToken) {
    SortedSet<InstallmentNoPII> set = new TreeSet<>();

    installments.forEach(installment ->
      mixedDpAdditionalDataList.forEach(mixedDpAdditionalData -> {
        Transfer transfer = installment.getTransfers().stream()
          .filter(t -> mixedDpAdditionalData.getTransferIndex()
            .equals(t.getTransferIndex())).findFirst()
          .orElseThrow(() -> new IllegalStateBusinessException(
            ErrorCodeConstants.ERROR_CODE_TRANSFER_NOT_FOUND,
            "There is no Transfer having transferIndex: [%s] associated with Installment having id: [%d].".formatted(
              mixedDpAdditionalData.getTransferIndex(),
              installment.getInstallmentId())));

        InstallmentNoPII technicalMixedDpInstallment = new InstallmentNoPII();
        technicalMixedDpInstallment.setStatus(
          isUnpaid ? InstallmentStatus.UNPAID : InstallmentStatus.PAID);
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
          transfer.getRemittanceInformation());
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

        if (!isUnpaid) {
          DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)
            .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_NOT_FOUND, "The DebtPositionTypeOrg with id " + debtPositionTypeOrgId + " was not found"));
          balanceResolverService.updateBalanceResolvingAmount(technicalMixedDpInstallment, organizationId, debtPositionTypeOrg, paymentDateTime, accessToken);
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
