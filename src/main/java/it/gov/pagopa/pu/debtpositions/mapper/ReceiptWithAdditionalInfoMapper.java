package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.UnknownDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReceiptWithAdditionalInfoMapper {
  public static final String UNKNOWN = "unknown";
  private final UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;

  public ReceiptWithAdditionalInfoMapper(UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService) {
    this.unknownDebtPositionTypeOrgRetrieverService = unknownDebtPositionTypeOrgRetrieverService;
  }

  public DebtPositionDTO mapToDebtPosition(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    OffsetDateTime now = OffsetDateTime.now();
    return DebtPositionDTO.builder()
      .organizationId(organization.getOrganizationId())
      .debtPositionOrigin(getDebtPositionOrigin(receiptDTO, organization))
      .debtPositionTypeOrgId(unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(organization.getOrganizationId())
        .getDebtPositionTypeOrgId())
      .iupdOrg(getIupdOrg(receiptDTO))
      .description(receiptDTO.getDescription())
      .status(DebtPositionStatus.PAID)
      .validityDate(null)
      .flagIuvVolatile(false)
      .multiDebtor(false)
      .flagPuPagoPaPayment(true)
      .creationDate(receiptDTO.getPaymentDateTime())
      .updateDate(now)
      .paymentOptions(List.of(PaymentOptionDTO.builder()
        .totalAmountCents(receiptDTO.getPaymentAmountCents())
        .status(PaymentOptionStatus.PAID)
        .description(receiptDTO.getDescription())
        .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.SINGLE_INSTALLMENT)
        .paymentOptionIndex(1)
        .installments(List.of(InstallmentDTO.builder()
          .status(InstallmentStatus.PAID)
          .syncStatus(null)
          .iupdPagopa(Utilities.generateRandomIupd(organization.getOrgFiscalCode()))
          .iud(receiptDTO.getIud())
          .iuv(receiptDTO.getCreditorReferenceId())
          .iuf(null)
          .iur(receiptDTO.getPaymentReceiptId())
          .nav(receiptDTO.getNoticeNumber())
          .dueDate(null)
          .notificationFeeCents(receiptDTO.getFeeCents())
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
          .creationDate(receiptDTO.getPaymentDateTime())
          .updateDate(now)
          .transfers(receiptDTO.getTransfers().stream().map(this::mapTransfer).toList())
          .build()))
        .build()))
      .build();
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
    return "PPR-"+receipt.getPaymentReceiptId();
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
      .build();
  }
}
