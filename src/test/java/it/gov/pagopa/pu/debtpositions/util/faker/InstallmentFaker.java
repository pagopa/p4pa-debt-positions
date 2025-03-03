package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.enums.PersonEntityType;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSyncStatusFaker.buildInstallmentSyncStatus;
import static it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.*;

public class InstallmentFaker {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);
  static LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

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
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .transfers(new ArrayList<>(List.of(buildTransfer())))
      .debtor(buildPerson())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(dateTime)
      .updateDate(dateTime)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .noPII(buildInstallmentNoPII())
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
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .personalDataId(123L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .debtorEntityType(PersonEntityType.F)
      .debtorFiscalCodeHash(new byte[] {})
      .balance("balance")
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(dateTime)
      .updateDate(dateTime)
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
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPerson())
      .transfers(new ArrayList<>(List.of(buildTransfer())))
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .creationDate(dateTime)
      .updateDate(dateTime)
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
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPersonDTO())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .transfers(new ArrayList<>(List.of(buildTransferDTO())))
      .creationDate(dateTime.atOffset(ZoneOffset.UTC))
      .updateDate(dateTime.atOffset(ZoneOffset.UTC))
      .build();
  }

  public static InstallmentDTO buildGeneratedIuvInstallmentDTO() {
    return InstallmentDTO.builder()
      .installmentId(1L)
      .paymentOptionId(1L)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("randomIUD")
      .iuv("generatedIuv")
      .iur("iur")
      .iuf("iuf")
      .nav("generatedNav")
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("1000.00")
      .debtor(buildPersonDTO())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .receiptId(1L)
      .transfers(new ArrayList<>(List.of(buildTransferDTO())))
      .creationDate(dateTime.atOffset(ZoneOffset.UTC))
      .updateDate(dateTime.atOffset(ZoneOffset.UTC))
      .build();
  }

  public static InstallmentDTO buildSyncInstallmentDTO(){
    return InstallmentDTO.builder()
      .iud("iud")
      .iuv("iuv")
      .dueDate(DATE)
      .paymentTypeCode("paymentTypeCode")
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPersonDTO())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(101L)
      .status(InstallmentStatus.UNPAID)
      .transfers(new ArrayList<>(List.of(buildSyncTransferDTO())))
      .build();
  }
}
