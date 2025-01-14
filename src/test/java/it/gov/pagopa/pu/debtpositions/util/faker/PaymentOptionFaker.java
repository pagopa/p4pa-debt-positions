package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.OFFSET_DATE_TIME;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;

public class PaymentOptionFaker {

  public static PaymentOptionDTO buildPaymentOptionDTO() {
    return PaymentOptionDTO.builder()
      .paymentOptionId(1L)
      .totalAmountCents(1L)
      .status("TO_SYNC")
      .multiDebtor(false)
      .dueDate(OFFSET_DATE_TIME)
      .description("description")
      .paymentOptionType(PaymentOptionDTO.PaymentOptionTypeEnum.DOWN_PAYMENT)
      .installments(List.of(buildInstallmentDTO()))
      .build();
  }

  public static PaymentOption buildPaymentOption() {
    return PaymentOption.builder()
      .paymentOptionId(1L)
      .totalAmountCents(1L)
      .status("TO_SYNC")
      .multiDebtor(false)
      .dueDate(OFFSET_DATE_TIME)
      .description("description")
      .paymentOptionType(PaymentOptionType.DOWN_PAYMENT)
      .installments(List.of(buildInstallmentNoPII()))
      .build();
  }
}
