package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

public class PaymentOptionFaker {

  private static final OffsetDateTime DATE = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

  public static PaymentOption buildPaymentOption() {
    PaymentOption paymentOption = new PaymentOption();
    paymentOption.setPaymentOptionId(123L);
    paymentOption.setTotalAmountCents(2000L);
    paymentOption.setDueDate(DATE);
    paymentOption.setStatus("PENDING");
    paymentOption.setMultiDebtor(true);
    paymentOption.setDescription("Payment description");
    paymentOption.setPaymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT);
    paymentOption.setInstallments(Collections.emptyList());
    return paymentOption;
  }

  public static PaymentOptionDTO buildPaymentOptionDTO() {
    PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();
    paymentOptionDTO.setPaymentOptionId(123L);
    paymentOptionDTO.setTotalAmountCents(2000L);
    paymentOptionDTO.setDueDate(DATE);
    paymentOptionDTO.setStatus("PENDING");
    paymentOptionDTO.setMultiDebtor(true);
    paymentOptionDTO.setDescription("Payment description");
    paymentOptionDTO.setPaymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.SINGLE_INSTALLMENT);
    paymentOptionDTO.setInstallments(Collections.emptyList());
    return paymentOptionDTO;
  }
}
