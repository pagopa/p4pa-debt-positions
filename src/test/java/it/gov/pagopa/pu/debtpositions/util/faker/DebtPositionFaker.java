package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionOriginRequest;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.*;

public class DebtPositionFaker {

  private static final OffsetDateTime DATE = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

  public static DebtPosition buildDebtPosition() {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(1L);
    debtPosition.setDebtPositionTypeOrgId(2L);
    debtPosition.setIupdOrg("IUPD_ORG");
    debtPosition.setDescription("Test Description");
    debtPosition.setStatus(DebtPositionStatus.TO_SYNC);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPosition.setIngestionFlowFileId(1001L);
    debtPosition.setIngestionFlowFileLineNumber(10L);
    debtPosition.setOrganizationId(500L);
    debtPosition.setNotificationDate(DATE);
    debtPosition.setValidityDate(DATE);
    debtPosition.setFlagIuvVolatile(true);
    debtPosition.setCreationDate(DATE.toLocalDateTime());
    debtPosition.setUpdateDate(DATE.toLocalDateTime());
    debtPosition.setPaymentOptions(new TreeSet<>(new ArrayList<>(List.of(buildPaymentOption()))));
    return debtPosition;
  }

  public static DebtPositionDTO buildDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.setIupdOrg("IUPD_ORG");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.setIngestionFlowFileId(1001L);
    debtPositionDTO.setIngestionFlowFileLineNumber(10L);
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setNotificationDate(DATE);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setCreationDate(DATE);
    debtPositionDTO.setUpdateDate(DATE);
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildPaymentOptionDTO())));
    return debtPositionDTO;
  }

  public static DebtPositionDTO buildGeneratedIuvDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.setIupdOrg("IUPD_ORG");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.setIngestionFlowFileId(1001L);
    debtPositionDTO.setIngestionFlowFileLineNumber(10L);
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setNotificationDate(DATE);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setCreationDate(DATE);
    debtPositionDTO.setUpdateDate(DATE);
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildGeneratedIuvPaymentOptionDTO())));
    return debtPositionDTO;
  }

  public static DebtPositionRequestDTO buildDebtPositionRequestDTO() {
    return DebtPositionRequestDTO.builder()
      .debtPositionId(1L)
      .debtPositionTypeOrgId(2L)
      .iupdOrg("IUPD_ORG")
      .description("Test Description")
      .status(DebtPositionRequestStatus.UNPAID)
      .debtPositionOrigin(DebtPositionOriginRequest.ORDINARY)
      .ingestionFlowFileId(1001L)
      .ingestionFlowFileLineNumber(10L)
      .organizationId(500L)
      .notificationDate(DATE)
      .validityDate(DATE)
      .flagIuvVolatile(true)
      .creationDate(DATE)
      .updateDate(DATE)
      .paymentOptions(List.of(buildPaymentOptionRequestDTO()))
      .build();
  }
}
