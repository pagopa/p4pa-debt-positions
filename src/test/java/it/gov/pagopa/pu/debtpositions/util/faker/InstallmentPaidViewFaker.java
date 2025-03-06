package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentPaidViewNoPII;

import java.time.OffsetDateTime;

public class InstallmentPaidViewFaker {

  private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

  public static InstallmentPaidViewNoPII mockInstanceInstallmentPaidViewNoPII(){
    return InstallmentPaidViewNoPII.builder()
      .installmentId(1L)
      .iuf("iuf")
      .iud("iud")
      .noticeNumber("123456")
      .orgFiscalCode("orgFiscalCode")
      .paymentReceiptId("paymentReceiptId")
      .paymentDateTime(OFFSET_DATE_TIME)
      .idPsp("1")
      .pspCompanyName("pspCompanyName")
      .paymentAmountCents(125L)
      .creditorReferenceId("creditorReferenceId")
      .amountCents(100L)
      .remittanceInformation("info")
      .category("category")
      .code("MARCA_BOLLO")
      .transferIndex(1)
      .feeCents(10L)
      .balance("balance")
      .companyName("company")
      .receiptPersonalDataId(123L)
      .build();
  }

}
