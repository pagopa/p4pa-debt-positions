package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.Action;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PersonEntityType;
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
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);


  public static InstallmentNoPII buildInstallmentNoPII(){
    return InstallmentNoPII.builder()
      .installmentId(100L)
      .paymentOptionId(10L)
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
      .iun("iun")
      .dueDate(DATE)
      .switchToExpired(true)
      .notificationFeeCents(1000L)
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
      .ingestionFlowFileAction(Action.I)
      .sourceFlowName("sourceFlowName")
      .receiptId(1L)
      .creationDate(DATETIME.toLocalDateTime())
      .updateDate(DATETIME.toLocalDateTime())
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .updateTraceId("TRACEID")
      .transfers(new TreeSet<>(List.of(buildTransfer())))
      .build();
  }

  public static InstallmentPIIDTO buildInstallmentPIIDTO(){
    return InstallmentPIIDTO.builder()
      .debtor(buildPerson())
      .build();
  }

  public static InstallmentDTO buildInstallmentDTO() {
    return InstallmentDTO.builder()
      .installmentId(100L)
      .paymentOptionId(10L)
      .status(InstallmentStatus.TO_SYNC)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("iud")
      .iuv("iuv")
      .iur("iur")
      .iuf("iuf")
      .nav("nav")
      .iun("iun")
      .dueDate(DATE)
      .switchToExpired(Boolean.TRUE)
      .notificationFeeCents(1000L)
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPerson())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .ingestionFlowFileAction(Action.I)
      .sourceFlowName("sourceFlowName")
      .receiptId(1L)
      .transfers(new ArrayList<>(List.of(buildTransferDTO())))
      .creationDate(DATETIME)
      .updateDate(DATETIME)
      .updateOperatorExternalId("OPERATOREXTERNALUSERID")
      .updateTraceId("TRACEID")
      .build();
  }

  public static InstallmentDTO buildGeneratedIuvInstallmentDTO() {
    return InstallmentDTO.builder()
      .installmentId(100L)
      .paymentOptionId(10L)
      .status(InstallmentStatus.UNPAID)
      .syncStatus(buildInstallmentSyncStatus())
      .iupdPagopa("iupdPagoPa")
      .iud("randomIUD")
      .iuv("generatedIuv")
      .iur("iur")
      .iuf("iuf")
      .nav("generatedNav")
      .iun("iun")
      .dueDate(DATE)
      .switchToExpired(Boolean.TRUE)
      .notificationFeeCents(1000L)
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("1000.00")
      .debtor(buildPerson())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(100L)
      .ingestionFlowFileAction(Action.I)
      .sourceFlowName("sourceFlowName")
      .receiptId(1L)
      .transfers(new ArrayList<>(List.of(buildTransferDTO())))
      .creationDate(DATETIME)
      .updateDate(DATETIME)
      .build();
  }

  public static InstallmentDTO buildSyncInstallmentDTO(){
    return InstallmentDTO.builder()
      .iud("iud")
      .iuv("iuv")
      .dueDate(DATE)
      .amountCents(100L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("balance")
      .debtor(buildPerson())
      .notificationDate(DATETIME)
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(101L)
      .ingestionFlowFileAction(Action.I)
      .status(InstallmentStatus.UNPAID)
      .sourceFlowName("ingestionFlowFileName")
      .transfers(new ArrayList<>(List.of(buildSyncTransferDTO())))
      .build();
  }
}
