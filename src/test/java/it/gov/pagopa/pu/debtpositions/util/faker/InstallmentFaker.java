package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPerson;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPersonDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;

public class InstallmentFaker {

  static LocalDateTime date = LocalDateTime.of(2025, 1, 1, 0,0,0, 0);
  static OffsetDateTime offsetDateTime = OffsetDateTime.of(date, ZoneOffset.UTC);

  public static Installment buildInstallment(){
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
      .dueDate(offsetDateTime)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .transfers(List.of(buildTransfer()))
      .debtor(buildPerson())
      .creationDate(date)
      .updateDate(date)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .build();
  }

  public static InstallmentNoPII buildInstallmentNoPII(){
    return InstallmentNoPII.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status("status")
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(offsetDateTime)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .debtorEntityType('F')
      .debtorFiscalCodeHash(new byte[] {})
      .balance("balance")
      .creationDate(date)
      .updateDate(date)
      .updateOperatorExternalId("OPERATOREXTERNALID")
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
      .dueDate(date)
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
    return InstallmentDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status("status")
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(date)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .debtor(buildPersonDTO())
      .transfers(List.of())
      .creationDate(date)
      .updateDate(date)
      .build();
  }

}
