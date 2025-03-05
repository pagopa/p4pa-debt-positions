package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOutcomeCode;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.enums.UniqueIdentifierType;
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
      .debtorEntityType(PersonEntityType.G)
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
      .personalDataId(123L)
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1(){
    return InstallmentPaidViewDTO.builder()
      .iuf("iuf")
      .flowRowNumber(1)
      .iud("iud")
      .iuv("123456")
      .domainIdentifier("orgFiscalCode")
      .receiptMessageIdentifier("paymentReceiptId")
      .receiptMessageDateTime(OFFSET_DATE_TIME)
      .requestMessageReference("paymentReceiptId")
      .requestDateTimeReference(OFFSET_DATE_TIME)
      .uniqueIdentifierType(UniqueIdentifierType.B)
      .uniqueIdentifierCode("1")
      .attestingName("pspCompanyName")
      .beneficiaryEntityType(PersonEntityType.G)
      .beneficiaryUniqueIdentifierCode("orgFiscalCode")
      .beneficiaryName("company")
      .payerUniqueIdentifierCode(PersonEntityType.G)
      .subjectPayingEntityType(PersonEntityType.F)
      .paymentOutcomeCode(PaymentOutcomeCode.PAYMENT_EXECUTED.getCode())
      .totalAmountPaidCents(125L)
      .uniquePaymentIdentifier("creditorReferenceId")
      .paymentContextCode("paymentReceiptId")
      .singleAmountPaidCents(100L)
      .singlePaymentOutcome("0")
      .singlePaymentOutcomeDateTime(OFFSET_DATE_TIME)
      .uniqueCollectionIdentifier("paymentReceiptId")
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1_1(){
    InstallmentPaidViewDTO installmentPaidViewDTO = mockInstanceInstallmentPaidViewDTO1();

    return installmentPaidViewDTO.toBuilder()
      .paymentReason("info")
      .collectionSpecificData("9/category")
      .dueType("code")
      .singlePaymentDataIndex(1)
      .pspAppliedFeesCents(10L)
      .receiptAttachmentType("BD")
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1_2(){
    InstallmentPaidViewDTO installmentPaidViewDTO = mockInstanceInstallmentPaidViewDTO1_1();

    return installmentPaidViewDTO.toBuilder()
      .balance("balance")
      .build();
  }

  public static InstallmentPaidViewDTO mockInstanceInstallmentPaidViewDTO1_3(){
    InstallmentPaidViewDTO installmentPaidViewDTO = mockInstanceInstallmentPaidViewDTO1_2();

    return installmentPaidViewDTO.toBuilder()
      .orgFiscalCode("orgFiscalCode")
      .orgName("companyName")
      .dueTaxonomicCode("category")
      .build();
  }

}
