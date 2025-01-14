package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;

import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPerson;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;

public class InstallmentFaker {

  public static Installment buildInstallment(){
    return Installment.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status("TO_SYNC")
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(OFFSET_DATE_TIME)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .transfers(List.of(buildTransfer()))
      .debtor(buildPerson())
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .updateOperatorExternalId(1L)
      .build();
  }

  public static InstallmentNoPII buildInstallmentNoPII(){
    return InstallmentNoPII.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status("TO_SYNC")
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(OFFSET_DATE_TIME)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .debtorEntityType('F')
      .debtorFiscalCodeHash(new byte[] {})
      .balance("balance")
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .updateOperatorExternalId(1L)
      .build();
  }

  public static InstallmentPIIDTO buildInstallmentPIIDTO(){
    return InstallmentPIIDTO.builder()
      .debtor(buildPerson())
      .build();
  }

  public static Installment buildInstallmentNoUpdate(){
    return Installment.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status("status")
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(date.atOffset(ZoneOffset.UTC))
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .debtor(buildPerson())
      .transfers(List.of())
      .creationDate(date)
      .updateDate(date)
      .build();
  }

  public static InstallmentDTO buildInstallmentDTO() {
    List<TransferDTO> transfers = new ArrayList<>();
    transfers.add(buildTransferDTO());
    return InstallmentDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status("TO_SYNC")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .iupdPagopa("iupdPagopa")
      .dueDate(OFFSET_DATE_TIME)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .debtor(buildPersonDTO())
      .transfers(transfers)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .build();
  }

}
