package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.OffsetDateTime;
import java.util.Collections;

class InstallmentMapperTest {

  private final TransferMapper transferMapper = new TransferMapper();
  private final InstallmentMapper installmentMapper = new InstallmentMapper(transferMapper);

  @Test
  void givenValidInstallment_WhenMapToDto_ThenReturnInstallmentDTO() {
    InstallmentNoPII installment = getInstallmentNoPII();

    InstallmentDTO result = installmentMapper.mapToDto(installment);

    assertNotNull(result);
    assertEquals(456L, result.getInstallmentId());
    assertEquals(123L, result.getPaymentOptionId());
    assertEquals("PAID", result.getStatus());
    assertEquals("IUD123", result.getIud());
    assertEquals("IUV123", result.getIuv());
    assertEquals("IUR123", result.getIur());
    assertEquals(OffsetDateTime.parse("2025-02-01T00:00:00+00:00"), result.getDueDate());
    assertEquals("PTC123", result.getPaymentTypeCode());
    assertEquals(1500L, result.getAmountCents());
    assertEquals(50L, result.getNotificationFeeCents());
    assertEquals("Remittance Info", result.getRemittanceInformation());
    assertEquals("Friendly Info", result.getHumanFriendlyRemittanceInformation());
    assertTrue(result.getTransfers().isEmpty());
    assertEquals(OffsetDateTime.parse("2025-01-01T00:00:00+00:00"), result.getCreationDate());
    assertEquals(OffsetDateTime.parse("2025-01-02T00:00:00+00:00"), result.getUpdateDate());
  }

  private static InstallmentNoPII getInstallmentNoPII() {
    InstallmentNoPII installment = new InstallmentNoPII();
    installment.setInstallmentId(456L);
    installment.setPaymentOptionId(123L);
    installment.setStatus("PAID");
    installment.setIud("IUD123");
    installment.setIuv("IUV123");
    installment.setIur("IUR123");
    installment.setDueDate(OffsetDateTime.parse("2025-02-01T00:00:00+00:00"));
    installment.setPaymentTypeCode("PTC123");
    installment.setAmountCents(1500L);
    installment.setNotificationFeeCents(50L);
    installment.setRemittanceInformation("Remittance Info");
    installment.setHumanFriendlyRemittanceInformation("Friendly Info");
    installment.setTransfers(Collections.emptyList());
    installment.setCreationDate(OffsetDateTime.parse("2025-01-01T00:00:00+00:00"));
    installment.setUpdateDate(OffsetDateTime.parse("2025-01-02T00:00:00+00:00"));
    return installment;
  }

  @Test
  void givenValidInstallmentDTO_WhenMapToModel_ThenReturnInstallment() {
    InstallmentDTO dto = InstallmentDTO.builder()
      .installmentId(456L)
      .paymentOptionId(123L)
      .status("PAID")
      .iud("IUD123")
      .iuv("IUV123")
      .iur("IUR123")
      .dueDate(OffsetDateTime.parse("2025-02-01T00:00:00+00:00"))
      .paymentTypeCode("PTC123")
      .amountCents(1500L)
      .notificationFeeCents(50L)
      .remittanceInformation("Remittance Info")
      .humanFriendlyRemittanceInformation("Friendly Info")
      .transfers(Collections.emptyList())
      .creationDate(OffsetDateTime.parse("2025-01-01T00:00:00+00:00"))
      .updateDate(OffsetDateTime.parse("2025-01-02T00:00:00+00:00"))
      .build();

    InstallmentNoPII result = installmentMapper.mapToModel(dto);

    assertNotNull(result);
    assertEquals(456L, result.getInstallmentId());
    assertEquals(123L, result.getPaymentOptionId());
    assertEquals("PAID", result.getStatus());
    assertEquals("IUD123", result.getIud());
    assertEquals("IUV123", result.getIuv());
    assertEquals("IUR123", result.getIur());
    assertEquals(OffsetDateTime.parse("2025-02-01T00:00:00+00:00"), result.getDueDate());
    assertEquals("PTC123", result.getPaymentTypeCode());
    assertEquals(1500, result.getAmountCents());
    assertEquals(50, result.getNotificationFeeCents());
    assertEquals("Remittance Info", result.getRemittanceInformation());
    assertEquals("Friendly Info", result.getHumanFriendlyRemittanceInformation());
    assertTrue(result.getTransfers().isEmpty());
    assertEquals(OffsetDateTime.parse("2025-01-01T00:00:00+00:00"), result.getCreationDate());
    assertEquals(OffsetDateTime.parse("2025-01-02T00:00:00+00:00"), result.getUpdateDate());
  }
}

