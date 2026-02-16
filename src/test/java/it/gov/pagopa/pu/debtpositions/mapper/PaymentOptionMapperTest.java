package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.mapper.pii.InstallmentPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentPIIDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

@ExtendWith(MockitoExtension.class)
class PaymentOptionMapperTest {

  @Mock
  private InstallmentPIIMapper installmentMapperMock;

  private PaymentOptionMapper paymentOptionMapper;

  @BeforeEach
  void setUp(){
    paymentOptionMapper = new PaymentOptionMapper(installmentMapperMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(installmentMapperMock);
  }

  @Test
  void givenValidPaymentOptionDTO_WhenMapToModel_ThenReturnPaymentOptionAndInstallmentMap() {
    PaymentOption paymentOptionExpected = buildPaymentOption();
    paymentOptionExpected.setStatus(PaymentOptionStatus.UNPAID);
    PaymentOptionDTO paymentOptionDTO = buildPaymentOptionDTO();
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();

    Mockito.when(installmentMapperMock.map(installmentDTO)).thenReturn(Pair.of(buildInstallmentNoPII(), buildInstallmentPIIDTO()));

    PaymentOption result = paymentOptionMapper.mapToModel(paymentOptionDTO);

    reflectionEqualsByName(paymentOptionExpected, result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    checkNotNullFields(result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }

  @Test
  void givenMapToDtoThenOk(){
    PaymentOption paymentOption = buildPaymentOption();

    PaymentOptionDTO paymentOptionExpected = buildPaymentOptionDTO();
    paymentOptionExpected.setStatus(PaymentOptionStatus.TO_SYNC);

    Mockito.when(installmentMapperMock.mapAll(paymentOption.getInstallments().stream().toList()))
      .thenReturn(paymentOptionExpected.getInstallments());

    PaymentOptionDTO result = paymentOptionMapper.mapToDto(paymentOption);
    System.out.println("result: "+result);

    reflectionEqualsByName(paymentOptionExpected, result);
    checkNotNullFields(result);

    // nested list should be modifiable
    result.getInstallments().clear();
  }

}
