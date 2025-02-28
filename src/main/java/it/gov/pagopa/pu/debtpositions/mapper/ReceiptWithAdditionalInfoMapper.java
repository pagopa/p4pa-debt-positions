package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.service.create.receipt.DebtPositionTypeOrgSecondaryOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReceiptWithAdditionalInfoMapper {
  public static final String UNKNOWN = "unknown";
  private final DebtPositionTypeOrgSecondaryOrgRetrieverService debtPositionTypeOrgSecondaryOrgRetrieverService;

  public ReceiptWithAdditionalInfoMapper(DebtPositionTypeOrgSecondaryOrgRetrieverService debtPositionTypeOrgSecondaryOrgRetrieverService) {
    this.debtPositionTypeOrgSecondaryOrgRetrieverService = debtPositionTypeOrgSecondaryOrgRetrieverService;
  }

  public DebtPositionDTO mapToDebtPosition(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    OffsetDateTime now = OffsetDateTime.now();
    return DebtPositionDTO.builder()
      .organizationId(organization.getOrganizationId())
      .debtPositionOrigin(DebtPositionOrigin.RECEIPT_PAGOPA)
      .debtPositionTypeOrgId(debtPositionTypeOrgSecondaryOrgRetrieverService.getSecondaryOrgDebtPositionTypeOrg(organization.getOrganizationId())
        .getDebtPositionTypeOrgId())
      .iupdOrg(getIupdOrg(receiptDTO))
      .description(receiptDTO.getDescription())
      .status(DebtPositionStatus.PAID)
      .validityDate(receiptDTO.getPaymentDateTime())
      .flagIuvVolatile(false)
      .multiDebtor(false)
      .flagPagoPaPayment(true)
      .creationDate(receiptDTO.getPaymentDateTime())
      .updateDate(now)
      .paymentOptions(List.of(PaymentOptionDTO.builder()
        .totalAmountCents(receiptDTO.getPaymentAmountCents())
        .status(PaymentOptionStatus.PAID)
        .dueDate(null)
        .description(receiptDTO.getDescription())
        .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.SINGLE_INSTALLMENT)
        .paymentOptionIndex(1)
        .installments(List.of(InstallmentDTO.builder()
          .status(InstallmentStatus.PAID)
          .syncStatus(null)
          .iupdPagopa(Utilities.generateRandomIupd(organization.getOrgFiscalCode()))
          .iud(Utilities.getRandomIUD())
          .iuv(receiptDTO.getCreditorReferenceId())
          .iuf(null)
          .iur(null)
          .nav(receiptDTO.getNoticeNumber())
          .dueDate(null)
          .paymentTypeCode(null)
          .amountCents(receiptDTO.getPaymentAmountCents())
          .remittanceInformation(getRemittanceInformation(receiptDTO))
          .balance(null)
          .legacyPaymentMetadata(getLegacyPaymentMetadata(receiptDTO))
          .debtor(receiptDTO.getDebtor())
          .notificationDate(null)
          .ingestionFlowFileId(null)
          .ingestionFlowFileLineNumber(null)
          .receiptId(receiptDTO.getReceiptId())
          .creationDate(receiptDTO.getPaymentDateTime())
          .updateDate(now)
          .transfers(receiptDTO.getTransfers().stream().map(this::mapTransfer).toList())
          .build()))
        .build()))
      .build();
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
      .transferId(transfer.getIdTransfer().longValue())
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
