package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

@ExtendWith(MockitoExtension.class)
class PaymentOptionMapperTest {

  @Mock
  private InstallmentPIIMapper installmentMapperMock;
  @Mock
  private InstallmentPIIMapper installmentPIIMapperMock;

  private PaymentOptionMapper paymentOptionMapper;

  @BeforeEach
  void setUp(){
    paymentOptionMapper = new PaymentOptionMapper(installmentMapperMock, installmentPIIMapperMock);
  }

  @Test
  void givenValidPaymentOptionDTO_WhenMapToModel_ThenReturnPaymentOptionAndInstallmentMap() {
    PaymentOption paymentOptionExpected = buildPaymentOption();
    paymentOptionExpected.setStatus(PaymentOptionStatus.UNPAID);
    PaymentOptionDTO paymentOptionDTO = buildPaymentOptionDTO();
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();

    Mockito.when(installmentPIIMapperMock.map(installmentDTO)).thenReturn(Pair.of(buildInstallmentNoPII(), buildInstallmentPIIDTO()));

    PaymentOption result = paymentOptionMapper.mapToModel(paymentOptionDTO);

    reflectionEqualsByName(paymentOptionExpected, result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
    checkNotNullFields(result, "creationDate", "updateDate", "updateOperatorExternalId", "updateTraceId");
  }

  @Test
  void givenMapToDtoThenOk(){
    PaymentOptionDTO paymentOptionExpected = buildPaymentOptionDTO();
    paymentOptionExpected.setStatus(PaymentOptionStatus.TO_SYNC);

    Mockito.when(installmentMapperMock.map(buildInstallmentNoPII())).thenReturn(buildInstallmentDTO());

    PaymentOptionDTO result = paymentOptionMapper.mapToDto(buildPaymentOption());
    System.out.println("result: "+result);

    reflectionEqualsByName(paymentOptionExpected, result);
    checkNotNullFields(result);
  }
}
