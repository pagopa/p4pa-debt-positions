package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentOptionRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentRequestDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentOptionMapperTest {

  @Mock
  private InstallmentRequestMapper installmentRequestMapperMock;

  @InjectMocks
  private final PaymentOptionRequestMapper mapper = Mappers.getMapper(PaymentOptionRequestMapper.class);

  @Test
  void testMapPaymentOptionDTO() {

    Mockito.when(installmentRequestMapperMock.map(buildInstallmentDTO()))
      .thenReturn(buildInstallmentRequestDTO());

    PaymentOptionRequestDTO paymentOption = mapper.map(buildPaymentOptionDTO());

    PaymentOptionRequestDTO expected = buildPaymentOptionRequestDTO();

    checkNotNullFields(paymentOption);
    assertEquals(expected, paymentOption);

  }
}
