package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

import java.util.List;
import java.util.TreeSet;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

public class DebtPositionFaker {

  public static DebtPositionDTO buildDebtPositionDTO() {
    return DebtPositionDTO.builder()
      .debtPositionId(1L)
      .iupdOrg("iupdOrg")
      .description("description")
      .status("TO_SYNC")
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(1L)
      .organizationId(1L)
      .debtPositionTypeOrgId(1L)
      .notificationDate(OFFSET_DATE_TIME)
      .validityDate(OFFSET_DATE_TIME)
      .flagIuvVolatile(false)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .paymentOptions(List.of(buildPaymentOptionDTO()))
      .build();
  }

  public static DebtPosition buildDebtPosition() {
    return DebtPosition.builder()
      .debtPositionId(1L)
      .iupdOrg("iupdOrg")
      .description("description")
      .status("TO_SYNC")
      .ingestionFlowFileId(1L)
      .ingestionFlowFileLineNumber(1L)
      .organizationId(1L)
      .debtPositionTypeOrgId(1L)
      .notificationDate(OFFSET_DATE_TIME)
      .validityDate(OFFSET_DATE_TIME)
      .flagIuvVolatile(false)
      .creationDate(OFFSET_DATE_TIME)
      .updateDate(OFFSET_DATE_TIME)
      .paymentOptions(List.of(buildPaymentOption()))
      .build();
  }
//  public static DebtPosition buildDebtPosition() {
//    DebtPosition debtPosition = new DebtPosition();
//    debtPosition.setDebtPositionId(1L);
//    debtPosition.setIupdOrg("IUPD_ORG");
//    debtPosition.setDescription("Test Description");
//    debtPosition.setStatus("ACTIVE");
//    debtPosition.setIngestionFlowFileId(1001L);
//    debtPosition.setIngestionFlowFileLineNumber(10L);
//    debtPosition.setOrganizationId(500L);
//    debtPosition.setDebtPositionTypeOrgId(200L);
//    debtPosition.setNotificationDate(DATE);
//    debtPosition.setValidityDate(DATE);
//    debtPosition.setFlagIuvVolatile(true);
//    debtPosition.setCreationDate(DATE.toLocalDateTime());
//    debtPosition.setUpdateDate(DATE.toLocalDateTime());
//    debtPosition.setPaymentOptions(new TreeSet<>(List.of(buildPaymentOption())));
//    return debtPosition;
//  }
}
