package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;

public class TransferFaker {

  public static Transfer buildTransfer() {
    Transfer transfer = new Transfer();
    transfer.setTransferId(123L);
    transfer.setOrgFiscalCode("ABC123");
    transfer.setOrgName("Organization Name");
    transfer.setAmountCents(1000L);
    transfer.setRemittanceInformation("Payment Info");
    transfer.setIban("IT60X0542811101000000123456");
    transfer.setPostalIban("123456");
    transfer.setCategory("Category");
    transfer.setTransferIndex(1L);
    transfer.setStamp(new Stamp("TYPE", "HASH", "PR"));
    return transfer;
  }

  public static TransferDTO buildTransferDTO() {
    return TransferDTO.builder()
      .transferId(123L)
      .orgFiscalCode("ABC123")
      .orgName("Organization Name")
      .amountCents(1000L)
      .remittanceInformation("Payment Info")
      .iban("IT60X0542811101000000123456")
      .postalIban("123456")
      .category("Category")
      .transferIndex(1L)
      .stampType("TYPE")
      .stampHashDocument("HASH")
      .stampProvincialResidence("PR")
      .build();
  }
}
