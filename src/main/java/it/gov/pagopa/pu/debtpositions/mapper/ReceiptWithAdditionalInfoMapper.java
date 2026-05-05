package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ReceiptWithAdditionalInfoMapper {
  public static final String UNKNOWN = "unknown";

  private static final String UNKNOWN_STATION_ID = "UNKNOWN";

  public DebtPositionDTO mapToDebtPosition(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization, Long debtPositionTypeOrgId, DebtPosition existingDp) {
    List<TransferDTO> techTransfers = buildTechTransfers(receiptDTO, organization);

    String iupdPagopa;
    String iud;
    String stationId;

    if (existingDp != null && !existingDp.getPaymentOptions().isEmpty() && !existingDp.getPaymentOptions().getFirst().getInstallments().isEmpty()) {
      InstallmentNoPII existingInstallment = existingDp.getPaymentOptions().getFirst().getInstallments().getFirst();
      iupdPagopa = existingInstallment.getIupdPagopa();
      iud = existingInstallment.getIud();
      stationId = existingDp.getStationId();
    } else {
      iupdPagopa = Utilities.generateRandomIupd(organization.getOrgFiscalCode());
      iud = receiptDTO.getIud() != null ? receiptDTO.getIud() : Utilities.getRandomIUD();
      stationId = UNKNOWN_STATION_ID;
    }

    return DebtPositionDTO.builder()
      .organizationId(organization.getOrganizationId())
      .debtPositionOrigin(getDebtPositionOrigin(receiptDTO, organization))
      .debtPositionTypeOrgId(debtPositionTypeOrgId)
      .iupdOrg(getIupdOrg(receiptDTO))
      .description(receiptDTO.getDescription())
      .status(DebtPositionStatus.PAID)
      .validityDate(null)
      .multiDebtor(false)
      .flagPuPagoPaPayment(true)
      .stationId(stationId)
      .paymentOptions(List.of(PaymentOptionDTO.builder()
        .totalAmountCents(receiptDTO.getPaymentAmountCents())
        .status(PaymentOptionStatus.PAID)
        .description(receiptDTO.getDescription())
        .paymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT)
        .paymentOptionIndex(1)
        .installments(List.of(InstallmentDTO.builder()
          .status(InstallmentStatus.PAID)
          .syncStatus(null)
          .iupdPagopa(iupdPagopa)
          .generateNotice(true)
          .iud(iud)
          .iuv(receiptDTO.getCreditorReferenceId())
          .iuf(null)
          .iur(receiptDTO.getPaymentReceiptId())
          .nav(receiptDTO.getNoticeNumber())
          .dueDate(null)
          .switchToExpired(false)
          .amountCents(receiptDTO.getPaymentAmountCents())
          .remittanceInformation(getRemittanceInformation(receiptDTO))
          .balance(receiptDTO.getBalance())
          .legacyPaymentMetadata(getLegacyPaymentMetadata(receiptDTO))
          .debtor(receiptDTO.getDebtor())
          .notificationDate(null)
          .ingestionFlowFileId(null)
          .ingestionFlowFileLineNumber(null)
          .receiptId(receiptDTO.getReceiptId())
          .sourceFlowName(receiptDTO.getSourceFlowName())
          .transfers(techTransfers)
          .build()))
        .build()))
      .build();
  }

  private List<TransferDTO> buildTechTransfers(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    Stream<ReceiptTransferDTO> techReceiptTransfers = receiptDTO.getTransfers().stream();
    if(!organization.getOrgFiscalCode().equals(receiptDTO.getOrgFiscalCode())){
      techReceiptTransfers = techReceiptTransfers
        .filter(rt -> organization.getOrgFiscalCode().equals(rt.getFiscalCodePA()));
    }
    return techReceiptTransfers.map(this::mapTransfer).toList();
  }

  /**
   * Rules for retrieving debtPositionOrigin from receipt:
   * in case receiptOrigin is RECEIPT_PAGOPA:
   * - if primary org -> RECEIPT_PAGOPA
   * - if secondary org -> SECONDARY_ORG
   * in all other cases it corresponds to receiptOrigin.
   */
  private DebtPositionOrigin getDebtPositionOrigin(ReceiptWithAdditionalNodeDataDTO receipt, Organization organization) {
    return switch (receipt.getReceiptOrigin()) {
      case RECEIPT_PAGOPA ->
        organization.getOrgFiscalCode().equals(receipt.getOrgFiscalCode()) ? DebtPositionOrigin.RECEIPT_PAGOPA : DebtPositionOrigin.SECONDARY_ORG;
      case PAYMENTS_REPORTING -> DebtPositionOrigin.REPORTING_PAGOPA;
      case RECEIPT_FILE -> DebtPositionOrigin.RECEIPT_FILE;
    };
  }

  private String getIupdOrg(ReceiptWithAdditionalNodeDataDTO receipt) {
    return "PPR-" + receipt.getPaymentReceiptId();
  }

  private String getRemittanceInformation(ReceiptWithAdditionalNodeDataDTO receipt) {
    return StringUtils.firstNonBlank(receipt.getTransfers().getFirst().getRemittanceInformation(), receipt.getDescription());
  }

  private String getLegacyPaymentMetadata(ReceiptWithAdditionalNodeDataDTO receipt) {
    return receipt.getMetadata() != null ? receipt.getMetadata().get("datiSpecificiRiscossione") : null;
  }

  private TransferDTO mapTransfer(ReceiptTransferDTO transfer) {
    return TransferDTO.builder()
      .orgFiscalCode(transfer.getFiscalCodePA())
      .orgName(transfer.getCompanyName())
      .amountCents(transfer.getTransferAmountCents())
      .remittanceInformation(transfer.getRemittanceInformation())
      //it's not possible from receipt to retrieve the stamp data
      .stampHashDocument(transfer.getMbdAttachment() != null ? UNKNOWN : null)
      .stampType(transfer.getMbdAttachment() != null ? UNKNOWN : null)
      .stampProvincialResidence(transfer.getMbdAttachment() != null ? UNKNOWN : null)
      .iban(transfer.getIban())
      .postalIban(null) //it's not possible to understand from receipt if it's postal or not
      .category(transfer.getTransferCategory())
      .transferIndex(transfer.getIdTransfer())
      .mbdAttachment(transfer.getMbdAttachment())
      .build();
  }
}
