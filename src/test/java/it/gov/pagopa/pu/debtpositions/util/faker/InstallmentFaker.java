package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.buildPerson;

public class InstallmentFaker {

  static OffsetDateTime date = OffsetDateTime.of(2025, 1, 1, 0,0,0, 0, ZoneOffset.UTC);

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
      .dueDate(date)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .notificationFeeCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .humanFriendlyRemittanceInformation("humanFriendlyRemittanceInformation")
      .balance("balance")
      .transfers(List.of())
      .debtor(buildPerson())
      .creationDate(date)
      .updateDate(date)
      .updateOperatorExternalId(1L)
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
      .dueDate(date)
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
      .updateOperatorExternalId(1L)
      .build();
  }

  public static InstallmentPIIDTO buildInstallmentPIIDTO(){
    return InstallmentPIIDTO.builder()
      .debtor(buildPerson())
      .build();
  }

}
