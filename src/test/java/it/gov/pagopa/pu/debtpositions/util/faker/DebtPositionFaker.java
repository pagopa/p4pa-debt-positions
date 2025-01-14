package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

public class DebtPositionFaker {

  private static final OffsetDateTime DATE = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

  public static DebtPosition buildDebtPosition() {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(1L);
    debtPosition.setIupdOrg("IUPD_ORG");
    debtPosition.setDescription("Test Description");
    debtPosition.setStatus("ACTIVE");
    debtPosition.setIngestionFlowFileId(1001L);
    debtPosition.setIngestionFlowFileLineNumber(10L);
    debtPosition.setOrganizationId(500L);
    debtPosition.setDebtPositionTypeOrgId(200L);
    debtPosition.setNotificationDate(DATE);
    debtPosition.setValidityDate(DATE);
    debtPosition.setFlagIuvVolatile(true);
    debtPosition.setCreationDate(DATE);
    debtPosition.setUpdateDate(DATE);
    debtPosition.setPaymentOptions(List.of(buildPaymentOption()));
    return debtPosition;
  }

  public static DebtPositionDTO buildDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setIupdOrg("IUPD_ORG");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setStatus("ACTIVE");
    debtPositionDTO.setIngestionFlowFileId(1001L);
    debtPositionDTO.setIngestionFlowFileLineNumber(10L);
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setDebtPositionTypeOrgId(200L);
    debtPositionDTO.setNotificationDate(DATE);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setCreationDate(DATE);
    debtPositionDTO.setUpdateDate(DATE);
    debtPositionDTO.setPaymentOptions(List.of(buildPaymentOptionDTO()));
    return debtPositionDTO;
  }
}
