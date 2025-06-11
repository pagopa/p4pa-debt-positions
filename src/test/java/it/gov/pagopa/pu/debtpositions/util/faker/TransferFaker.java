package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TransferFaker {
  private static final LocalDate DATE = LocalDate.of(2099, 1, 3);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

  public static Transfer buildTransfer() {
    Transfer transfer = new Transfer();
    transfer.setTransferId(1000L);
    transfer.setInstallmentId(100L);
    transfer.setOrgFiscalCode("12345678903");
    transfer.setOrgName("Organization Name");
    transfer.setAmountCents(1000L);
    transfer.setRemittanceInformation("Payment Info");
    transfer.setIban("IT60X0542811101000000123456");
    transfer.setPostalIban("IT60X0542811101009000123456");
    transfer.setCategory("category");
    transfer.setTransferIndex(1);
    transfer.setStamp(new Stamp("TYPE", "HASH", "PR"));
    transfer.setCreationDate(DATETIME.toLocalDateTime());
    transfer.setUpdateDate(DATETIME.toLocalDateTime());
    transfer.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    transfer.setUpdateTraceId("TRACEID");
    return transfer;
  }

  public static TransferDTO buildTransferDTO() {
    return TransferDTO.builder()
      .transferId(1000L)
      .installmentId(100L)
      .orgFiscalCode("12345678903")
      .orgName("Organization Name")
      .amountCents(1000L)
      .remittanceInformation("Payment Info")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101009000123456")
      .category("category")
      .transferIndex(1)
      .stampType("TYPE")
      .stampHashDocument("HASH")
      .stampProvincialResidence("PR")
      .creationDate(DATETIME)
      .updateDate(DATETIME)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .updateTraceId("TRACEID")
      .build();
  }

  public static TransferDTO buildSyncTransferDTO(){
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
}
