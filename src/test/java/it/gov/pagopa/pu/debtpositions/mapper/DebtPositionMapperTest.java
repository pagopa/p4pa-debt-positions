package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.ArrayList;

class DebtPositionMapperTest {

  private final PaymentOptionMapper paymentOptionMapper = new PaymentOptionMapper(new InstallmentMapper(new TransferMapper()));
  private final DebtPositionMapper debtPositionMapper = new DebtPositionMapper(paymentOptionMapper);

  @Test
  void givenValidDebtPosition_whenMapToDto_thenReturnDebtPositionDTO() {
    DebtPosition debtPosition = getDebtPosition();

    DebtPositionDTO result = debtPositionMapper.mapToDto(debtPosition);

    assertNotNull(result);
    assertEquals(1L, result.getDebtPositionId());
    assertEquals("IUPD_ORG", result.getIupdOrg());
    assertEquals("Test Description", result.getDescription());
    assertEquals("ACTIVE", result.getStatus());
    assertEquals(1001L, result.getIngestionFlowFileId());
    assertEquals(10L, result.getIngestionFlowFileLineNumber());
    assertEquals(500L, result.getOrganizationId());
    assertEquals(200L, result.getDebtPositionTypeOrgId());
    assertEquals(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"), result.getNotificationDate());
    assertEquals(OffsetDateTime.parse("2025-06-01T10:00:00+00:00"), result.getValidityDate());
    assertTrue(result.getFlagIuvVolatile());
    assertEquals(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"), result.getCreationDate());
    assertEquals(OffsetDateTime.parse("2025-01-02T10:00:00+00:00"), result.getUpdateDate());
    assertTrue(result.getPaymentOptions().isEmpty());
  }

  private static DebtPosition getDebtPosition() {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(1L);
    debtPosition.setIupdOrg("IUPD_ORG");
    debtPosition.setDescription("Test Description");
    debtPosition.setStatus("ACTIVE");
    debtPosition.setIngestionFlowFileId(1001L);
    debtPosition.setIngestionFlowFileLineNumber(10L);
    debtPosition.setOrganizationId(500L);
    debtPosition.setDebtPositionTypeOrgId(200L);
    debtPosition.setNotificationDate(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"));
    debtPosition.setValidityDate(OffsetDateTime.parse("2025-06-01T10:00:00+00:00"));
    debtPosition.setFlagIuvVolatile(true);
    debtPosition.setCreationDate(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"));
    debtPosition.setUpdateDate(OffsetDateTime.parse("2025-01-02T10:00:00+00:00"));
    debtPosition.setUpdateOperatorExternalId(300L);
    debtPosition.setPaymentOptions(Collections.emptyList());
    return debtPosition;
  }

  @Test
  void givenValidDebtPositionDTO_whenMapToModel_thenReturnDebtPosition() {
    DebtPositionDTO dto = getDebtPositionDTO();

    DebtPosition result = debtPositionMapper.mapToModel(dto);

    assertNotNull(result);
    assertEquals(1L, result.getDebtPositionId());
    assertEquals("IUPD_ORG", result.getIupdOrg());
    assertEquals("Test Description", result.getDescription());
    assertEquals("ACTIVE", result.getStatus());
    assertEquals(1001L, result.getIngestionFlowFileId());
    assertEquals(10L, result.getIngestionFlowFileLineNumber());
    assertEquals(500L, result.getOrganizationId());
    assertEquals(200L, result.getDebtPositionTypeOrgId());
    assertEquals(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"), result.getNotificationDate());
    assertEquals(OffsetDateTime.parse("2025-06-01T10:00:00+00:00"), result.getValidityDate());
    assertTrue(result.isFlagIuvVolatile());
    assertEquals(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"), result.getCreationDate());
    assertEquals(OffsetDateTime.parse("2025-01-02T10:00:00+00:00"), result.getUpdateDate());
    assertNotNull(result.getPaymentOptions());
    assertTrue(result.getPaymentOptions().isEmpty());
  }

  private static DebtPositionDTO getDebtPositionDTO() {
    DebtPositionDTO dto = new DebtPositionDTO();
    dto.setDebtPositionId(1L);
    dto.setIupdOrg("IUPD_ORG");
    dto.setDescription("Test Description");
    dto.setStatus("ACTIVE");
    dto.setIngestionFlowFileId(1001L);
    dto.setIngestionFlowFileLineNumber(10L);
    dto.setOrganizationId(500L);
    dto.setDebtPositionTypeOrgId(200L);
    dto.setNotificationDate(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"));
    dto.setValidityDate(OffsetDateTime.parse("2025-06-01T10:00:00+00:00"));
    dto.setFlagIuvVolatile(true);
    dto.setCreationDate(OffsetDateTime.parse("2025-01-01T10:00:00+00:00"));
    dto.setUpdateDate(OffsetDateTime.parse("2025-01-02T10:00:00+00:00"));
    dto.setPaymentOptions(new ArrayList<>());
    return dto;
  }
}
