package it.gov.pagopa.pu.debtpositions.util.faker;

import com.fasterxml.jackson.databind.node.NullNode;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferSynchronizeDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class InstallmentSynchronizeFaker {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

  public static InstallmentSynchronizeDTO buildInstallmentSynchronizeDTO(){
    return InstallmentSynchronizeDTO.builder()
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(101L)
      .organizationId(1L)
      .action(InstallmentSynchronizeDTO.ActionEnum.I)
      .draft(Boolean.FALSE)
      .iupdOrg("IUPD_ORG")
      .description("Test Description")
      .validityDate(DATE)
      .multiDebtor(Boolean.TRUE)
      .notificationDate(DATETIME)
      .paymentOptionIndex(1)
      .paymentOptionType("SINGLE_INSTALLMENT")
      .paymentOptionDescription("Payment description")
      .iud("iud")
      .iuv("iuv")
      .entityType(InstallmentSynchronizeDTO.EntityTypeEnum.F)
      .fiscalCode("uniqueIdentifierCode")
      .fullName("fullName")
      .address("address")
      .civic("civic")
      .postalCode("postalCode")
      .location("location")
      .province("province")
      .nation("nation")
      .email("email@test.it")
      .dueDate(DATE)
      .amountCents(10000L)
      .debtPositionTypeCode("TEST_CODE")
      .notificationFeeCents(1000L)
      .remittanceInformation("remittanceInformation")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .flagPagoPaPayment(Boolean.TRUE)
      .balance("balance")
      .flagMultibeneficiary(Boolean.TRUE)
      .numberBeneficiary(2)
      .additionalTransfers(new ArrayList<>(List.of(buildTransferSynchronizeDTO())))
      .executionConfig(NullNode.instance)
      .build();
  }

  public static TransferSynchronizeDTO buildTransferSynchronizeDTO(){
    return TransferSynchronizeDTO.builder()
      .orgFiscalCode("orgFiscalCode2")
      .orgName("orgName2")
      .amountCents(50L)
      .remittanceInformation("remittanceInformation2")
      .iban("iban2")
      .category("category2")
      .transferIndex(2)
      .build();
  }
}
