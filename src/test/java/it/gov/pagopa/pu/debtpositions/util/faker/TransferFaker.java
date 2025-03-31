package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;

public class TransferFaker {

  public static Transfer buildTransfer() {
    Transfer transfer = new Transfer();
    transfer.setTransferId(1000L);
    transfer.setInstallmentId(100L);
    transfer.setOrgFiscalCode("12345678903");
    transfer.setOrgName("Organization Name");
    transfer.setAmountCents(1000L);
    transfer.setRemittanceInformation("Payment Info");
    transfer.setIban("IT60X0542811101000000123456");
    transfer.setPostalIban("123456");
    transfer.setCategory("category");
    transfer.setTransferIndex(2);
    transfer.setStamp(new Stamp("TYPE", "HASH", "PR"));
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
      .postalIban("123456")
      .category("category")
      .transferIndex(2)
      .stampType("TYPE")
      .stampHashDocument("HASH")
      .stampProvincialResidence("PR")
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
