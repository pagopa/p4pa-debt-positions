package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.*;
import static org.junit.jupiter.api.Assertions.*;

class PaymentOptionMapperTest {

  private final PaymentOptionMapper paymentOptionMapper = new PaymentOptionMapper();

  @Test
  void givenValidPaymentOptionDTO_WhenMapToModel_ThenReturnPaymentOption() {
    PaymentOption paymentOptionExpected = buildPaymentOption();
    PaymentOptionDTO paymentOptionDTO = buildPaymentOptionDTO();

    PaymentOption result = paymentOptionMapper.mapToModel(paymentOptionDTO);

    assertEquals(paymentOptionExpected, result);
  }
}
