package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

@ExtendWith(MockitoExtension.class)
class PaymentOptionMapperTest {

  @Mock
  private InstallmentMapper installmentMapperMock;
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

    Mockito.when(installmentMapperMock.mapToModel(buildInstallmentDTO())).thenReturn(buildInstallment());

    Mockito.when(installmentPIIMapperMock.map(buildInstallment())).thenReturn(Pair.of(buildInstallmentNoPII(), buildInstallmentPIIDTO()));

    Pair<PaymentOption, Map<InstallmentNoPII, Installment>> result = paymentOptionMapper.mapToModel(paymentOptionDTO);

    reflectionEqualsByName(paymentOptionExpected, result.getFirst());
    checkNotNullFields(result.getFirst(), "updateOperatorExternalId", "creationDate", "updateDate");
  }

  @Test
  void givenMapToDtoThenOk(){
    PaymentOptionDTO paymentOptionExpected = buildPaymentOptionDTO();
    paymentOptionExpected.setStatus(PaymentOptionStatus.TO_SYNC);

    Mockito.when(installmentMapperMock.mapToDto(buildInstallmentNoPII())).thenReturn(buildInstallmentDTO());

    PaymentOptionDTO result = paymentOptionMapper.mapToDto(buildPaymentOption());
    System.out.println("result: "+result);

    reflectionEqualsByName(paymentOptionExpected, result);
    checkNotNullFields(result);
  }
}
