package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.Stamp;
import it.gov.pagopa.pu.debtpositions.model.Transfer;

public class TransferFaker {

    public static TransferDTO buildTransferDTO(){
        return TransferDTO.builder()
          .transferId(1L)
          .orgFiscalCode("orgFiscalCode")
          .orgName("orgName")
          .amountCents(100L)
          .remittanceInformation("remittanceInformation")
          .stampType("stampType")
          .stampHashDocument("stampHashDocument")
          .stampProvincialResidence("stampProvincialResidence")
          .iban("iban")
          .postalIban("postalIban")
          .category("category")
          .transferIndex(1)
          .build();
    }

  public static Transfer buildTransfer(){
    return Transfer.builder()
      .transferId(1L)
      .orgFiscalCode("orgFiscalCode")
      .orgName("orgName")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .stamp(new Stamp())
      .iban("iban")
      .postalIban("postalIban")
      .category("category")
      .transferIndex(1L)
      .build();
  }
}
