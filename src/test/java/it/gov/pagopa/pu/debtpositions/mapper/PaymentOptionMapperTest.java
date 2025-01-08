package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class PaymentOptionMapperTest {

  private final TransferMapper transferMapper = new TransferMapper();
  private final InstallmentMapper installmentMapper = new InstallmentMapper(transferMapper);
  private final PaymentOptionMapper paymentOptionMapper = new PaymentOptionMapper(installmentMapper);

  @Test
  void givenValidPaymentOption_WhenMapToDto_ThenReturnPaymentOptionDTO() {
    OffsetDateTime dueDate = OffsetDateTime.parse("2025-01-01T00:00:00+00:00");

    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(123L);
    paymentOption.setTotalAmountCents(2000L);
    paymentOption.setDueDate(dueDate);
    paymentOption.setStatus("PENDING");
    paymentOption.setMultiDebtor(true);
    paymentOption.setDescription("Payment description");
    paymentOption.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOption.setInstallments(Collections.emptyList());

    PaymentOptionDTO result = paymentOptionMapper.mapToDto(paymentOption);

    assertNotNull(result);
    assertEquals(123L, result.getPaymentOptionId());
    assertEquals(2000, result.getTotalAmountCents());
    assertEquals(dueDate, result.getDueDate());
    assertEquals("PENDING", result.getStatus());
    assertTrue(result.getMultiDebtor());
    assertEquals("Payment description", result.getDescription());
    assertEquals(PaymentOptionDTO.PaymentOptionTypeEnum.SINGLE_INSTALLMENT, result.getPaymentOptionType());
    assertTrue(result.getInstallments().isEmpty());
  }

  @Test
  void givenValidPaymentOptionDTO_WhenMapToModel_ThenReturnPaymentOption() {
    OffsetDateTime dueDate = OffsetDateTime.parse("2025-01-01T00:00:00+00:00");

    PaymentOptionDTO dto = PaymentOptionDTO.builder()
      .paymentOptionId(123L)
      .totalAmountCents(2000L)
      .dueDate(dueDate)
      .status("PENDING")
      .multiDebtor(true)
      .description("Payment description")
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.DOWN_PAYMENT)
      .installments(Collections.emptyList())
      .build();

    PaymentOption result = paymentOptionMapper.mapToModel(dto);

    assertNotNull(result);
    assertEquals(123L, result.getPaymentOptionId());
    assertEquals(2000L, result.getTotalAmountCents());
    assertEquals(dueDate, result.getDueDate());
    assertEquals("PENDING", result.getStatus());
    assertTrue(result.isMultiDebtor());
    assertEquals("Payment description", result.getDescription());
    assertEquals(PaymentOptionType.DOWN_PAYMENT, result.getPaymentOptionType());
    assertTrue(result.getInstallments().isEmpty());
  }
}
