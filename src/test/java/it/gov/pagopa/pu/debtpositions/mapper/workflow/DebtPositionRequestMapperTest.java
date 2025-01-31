package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionRequestDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionRequestMapperTest {
  @Mock
  private PaymentOptionRequestMapper paymentOptionRequestMapperMock;

  @InjectMocks
  private final DebtPositionRequestMapper mapper = Mappers.getMapper(DebtPositionRequestMapper.class);

  @Test
  void testMapDebtPosition() {
    DebtPositionRequestDTO expected = buildDebtPositionRequestDTO();

    Mockito.when(paymentOptionRequestMapperMock.map(buildPaymentOptionDTO())).thenReturn(buildPaymentOptionRequestDTO());

    DebtPositionRequestDTO result = mapper.map(buildDebtPositionDTO());

    assertEquals(expected, result);
    checkNotNullFields(result);
  }
}
