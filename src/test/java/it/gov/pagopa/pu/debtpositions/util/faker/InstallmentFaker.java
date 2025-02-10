package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSyncStatusFaker.buildInstallmentSyncStatus;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.*;

public class InstallmentFaker {

  static LocalDateTime date = LocalDateTime.now().plusDays(2);
  static OffsetDateTime offsetDateTime = OffsetDateTime.of(date, ZoneOffset.UTC);

  public static Installment buildInstallment(){
    return Installment.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentStatus.TO_SYNC)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(offsetDateTime)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .transfers(new ArrayList<>(List.of(buildTransfer())))
      .debtor(buildPerson())
      .notificationDate(offsetDateTime)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(date)
      .updateDate(date)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .build();
  }

  public static InstallmentNoPII buildInstallmentNoPII(){
    return InstallmentNoPII.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentStatus.TO_SYNC)
      .syncStatus(InstallmentSyncStatus.builder()
        .syncStatusFrom(InstallmentStatus.DRAFT)
        .syncStatusTo(InstallmentStatus.UNPAID).build())
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(offsetDateTime)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .debtorEntityType(PersonEntityType.F)
      .debtorFiscalCodeHash(new byte[] {})
      .balance("balance")
      .notificationDate(offsetDateTime)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(date)
      .updateDate(date)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .transfers(new TreeSet<>(List.of(buildTransfer())))
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
      .status(InstallmentStatus.TO_SYNC)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(date.atOffset(ZoneOffset.UTC))
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPerson())
      .transfers(new ArrayList<>(List.of(buildTransfer())))
      .notificationDate(offsetDateTime)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(date)
      .updateDate(date)
      .build();
  }

  public static InstallmentDTO buildInstallmentDTO() {
    return InstallmentDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .dueDate(date.atOffset(ZoneOffset.UTC))
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPersonDTO())
      .notificationDate(offsetDateTime)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .transfers(new ArrayList<>(List.of(buildTransferDTO())))
      .creationDate(date.atOffset(ZoneOffset.UTC))
      .updateDate(date.atOffset(ZoneOffset.UTC))
      .build();
  }

  public static InstallmentDTO buildGeneratedIuvInstallmentDTO() {
    return InstallmentDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("generatedIuv")
      .iur("iur")
      .iuf("iuf")
      .nav("generatedNav")
      .dueDate(date.atOffset(ZoneOffset.UTC))
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPersonDTO())
      .notificationDate(offsetDateTime)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .transfers(new ArrayList<>(List.of(buildTransferDTO())))
      .creationDate(date.atOffset(ZoneOffset.UTC))
      .updateDate(date.atOffset(ZoneOffset.UTC))
      .build();
  }

}
