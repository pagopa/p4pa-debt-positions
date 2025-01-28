package it.gov.pagopa.pu.debtpositions.enums;

import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentOptionTypeEnumTest {

  @Test
  void testConversion(){
    for (PaymentOptionType value : PaymentOptionType.values()) {
      Assertions.assertDoesNotThrow(() -> PaymentOptionDTO.PaymentOptionTypeEnum.valueOf(value.name()));
    }
  }
}
