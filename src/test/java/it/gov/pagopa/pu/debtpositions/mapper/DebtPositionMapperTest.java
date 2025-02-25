package it.gov.pagopa.pu.debtpositions.mapper;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;

import it.gov.pagopa.pu.debtpositions.dto.DebtPositionWithType;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

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

  @Test
  void givenMapToDtoThenOk(){
    DebtPositionDTO debtPositionExpected = buildDebtPositionDTO();
    debtPositionExpected.setStatus(DebtPositionStatus.TO_SYNC);

    Mockito.when(paymentOptionMapperMock.mapToDto(buildPaymentOption())).thenReturn(buildPaymentOptionDTO());

    DebtPositionDTO result = debtPositionMapper.mapToDto(buildDebtPosition());

    checkNotNullFields(result);
    reflectionEqualsByName(debtPositionExpected, result);
  }

  @Test
  void givenMultiDebtorDebtPositionDetailWhenMapToDebtPositionDetailDTOThenOk(){
    DebtPositionDTO debtPositionExpected = buildDebtPositionDTO();
    debtPositionExpected.setMultiDebtor(true);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.UNPAID);
    debtPosition.setMultiDebtor(true);
    String debtPositionTypeOrgDescription = "debtPositionTypeOrgDescription";
    String debtPositionTypeOrgCode = "debtPositionTypeOrgCode";

    Mockito.when(paymentOptionMapperMock.mapToDto(buildPaymentOption())).thenReturn(buildPaymentOptionDTO());

    DebtPositionDetailDTO result = debtPositionMapper.mapToDebtPositionDetailDTO(
      new DebtPositionWithType(debtPosition,debtPositionTypeOrgDescription,debtPositionTypeOrgCode));

    Assertions.assertNotNull(result);
    checkNotNullFields(result);
    reflectionEqualsByName(debtPositionExpected, result);
    Assertions.assertEquals(debtPositionTypeOrgDescription,result.getDebtPositionTypeOrgDescription());
    Assertions.assertEquals(debtPositionTypeOrgCode,result.getDebtPositionTypeOrgCode());
    Assertions.assertNotNull(result.getDebtor());
    Assertions.assertEquals("CO-OBBLIGATO",result.getDebtor().getFullName());
    Assertions.assertNull(result.getDebtor().getAddress());
    Assertions.assertNull(result.getDebtor().getCivic());
    Assertions.assertNull(result.getDebtor().getEmail());
    Assertions.assertNull(result.getDebtor().getLocation());
    Assertions.assertNull(result.getDebtor().getEntityType());
    Assertions.assertNull(result.getDebtor().getFiscalCode());
    Assertions.assertNull(result.getDebtor().getNation());
    Assertions.assertNull(result.getDebtor().getPostalCode());
    Assertions.assertNull(result.getDebtor().getProvince());
  }

  @Test
  void givenNoMultiDebtorDebtPositionDetailWhenMapToDebtPositionDetailDTOThenOk(){
    DebtPositionDTO debtPositionExpected = buildDebtPositionDTO();
    debtPositionExpected.setMultiDebtor(false);
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.UNPAID);
    debtPosition.setMultiDebtor(false);
    String debtPositionTypeOrgDescription = "debtPositionTypeOrgDescription";
    String debtPositionTypeOrgCode = "debtPositionTypeOrgCode";

    Mockito.when(paymentOptionMapperMock.mapToDto(buildPaymentOption())).thenReturn(buildPaymentOptionDTO());

    DebtPositionDetailDTO result = debtPositionMapper.mapToDebtPositionDetailDTO(
      new DebtPositionWithType(debtPosition,debtPositionTypeOrgDescription,debtPositionTypeOrgCode));

    Assertions.assertNotNull(result);
    checkNotNullFields(result);
    reflectionEqualsByName(debtPositionExpected, result);
    Assertions.assertEquals(debtPositionTypeOrgDescription,result.getDebtPositionTypeOrgDescription());
    Assertions.assertEquals(debtPositionTypeOrgCode,result.getDebtPositionTypeOrgCode());
    checkNotNullFields(result.getDebtor());
    reflectionEqualsByName(
      debtPositionExpected.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor(),
      result.getDebtor());
  }
}
