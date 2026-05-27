package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.MixedTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

public class TransferFaker {
  private static final LocalDate DATE = LocalDate.of(2099, 1, 3);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

  public static Transfer buildTransfer() {
    Transfer transfer = new Transfer();
    transfer.setTransferId(1000L);
    transfer.setInstallmentId(100L);
    transfer.setOrgFiscalCode("12345678901");
    transfer.setOrgName("Organization Name");
    transfer.setAmountCents(1100L);
    transfer.setRemittanceInformation("Payment Info");
    transfer.setIban("IT60X0542811101000000123456");
    transfer.setPostalIban("IT60X0542811101009000123456");
    transfer.setCategory("001122233");
    transfer.setTransferIndex(1);
    transfer.setCreationDate(DATETIME.toLocalDateTime());
    transfer.setUpdateDate(DATETIME.toLocalDateTime());
    transfer.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    transfer.setUpdateTraceId("TRACEID");
    transfer.setMbdAttachment("MBD_ATTACHMENT");
    transfer.setFlagOwner(true);
    return transfer;
  }

  public static TransferDTO buildTransferDTO() {
    return TransferDTO.builder()
      .transferId(1000L)
      .installmentId(100L)
      .orgFiscalCode("12345678901")
      .orgName("Organization Name")
      .amountCents(1100L)
      .remittanceInformation("Payment Info")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101009000123456")
      .category("001122233")
      .transferIndex(1)
      .creationDate(DATETIME)
      .updateDate(DATETIME)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .updateTraceId("TRACEID")
      .mbdAttachment("MBD_ATTACHMENT")
      .flagOwner(true)
      .build();
  }

  public static TransferDTO buildSyncTransferDTO() {
    return TransferDTO.builder()
      .orgFiscalCode("orgFiscalCode2")
      .orgName("orgName2")
      .amountCents(50L)
      .remittanceInformation("remittanceInformation2")
      .iban("iban2")
      .category("category2")
      .transferIndex(2)
      .build();
  }

  public static MixedTransferDTO buildMixedTransferDTO() {
    return MixedTransferDTO.builder()
      .iud("IUD")
      .debtPositionTypeOrgId(100L)
      .amountCents(50L)
      .balance("Test Balance")
      .stampType("stampType")
      .stampHashDocument("stampHashDocument")
      .stampProvincialResidence("stampProvincialResidence")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101000000123456")
      .legacyPaymentMetadata("9/01234567/xxxxx")
      .remittanceInformation("Payment Info")
      .build();
  }

  public static List<Transfer> buildMixedTransferList(int size) {
    return IntStream.rangeClosed(1, size).mapToObj(TransferFaker::buildMixedTransfer).toList();
  }

  public static Transfer buildMixedTransfer(int i) {
    Transfer transfer = new Transfer();
    transfer.setTransferId(1000L * i);
    transfer.setInstallmentId(100L);
    transfer.setOrgFiscalCode("12345678901");
    transfer.setOrgName("Organization Name");
    transfer.setAmountCents(100L);
    transfer.setRemittanceInformation("Payment Info");
    transfer.setIban("IT60X0542811101000000123456");
    transfer.setPostalIban("IT60X0542811101009000123456");
    transfer.setCategory("001122233");
    transfer.setTransferIndex(i);
    transfer.setStamp(
      new Stamp("stampType", "stampHashDocument", "stampProvincialResidence"));
    transfer.setCreationDate(DATETIME.toLocalDateTime());
    transfer.setUpdateDate(DATETIME.toLocalDateTime());
    transfer.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    transfer.setUpdateTraceId("TRACEID");
    transfer.setMbdAttachment("MBD_ATTACHMENT");
    return transfer;
  }
}
