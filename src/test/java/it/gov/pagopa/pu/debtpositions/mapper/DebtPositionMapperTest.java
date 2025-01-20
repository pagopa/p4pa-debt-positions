package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

@ExtendWith(MockitoExtension.class)
class DebtPositionMapperTest {

  @Mock
  private PaymentOptionMapper paymentOptionMapperMock;

  private DebtPositionMapper debtPositionMapper;

  @BeforeEach
  void setUp(){
    debtPositionMapper = new DebtPositionMapper(paymentOptionMapperMock);
  }

  @Test
  void givenValidDebtPositionDTO_whenMapToModel_thenReturnDebtPositionAndInstallmentMap() {
    DebtPosition debtPositionExpected = buildDebtPosition();
    debtPositionExpected.setStatus(DebtPositionStatus.UNPAID);
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Map<InstallmentNoPII, Installment> installmentMap = new HashMap<>();
    installmentMap.put(buildInstallmentNoPII(), buildInstallment());

    PaymentOption paymentOption = buildPaymentOption();
    Pair<PaymentOption, Map<InstallmentNoPII, Installment>> paymentOptionPair = Pair.of(paymentOption, installmentMap);

    Mockito.when(paymentOptionMapperMock.mapToModel(buildPaymentOptionDTO())).thenReturn(paymentOptionPair);

    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> result = debtPositionMapper.mapToModel(debtPositionDTO);

    reflectionEqualsByName(debtPositionExpected, result.getFirst());
    checkNotNullFields(result.getFirst(), "updateOperatorExternalId");
  }

}
