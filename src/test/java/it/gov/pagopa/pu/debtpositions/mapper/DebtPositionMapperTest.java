package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtPositions;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void givenPagedDebtPositionsThenOk(){
    DebtPosition debtPosition = buildDebtPosition();
    debtPosition.setStatus(DebtPositionStatus.UNPAID);

    Mockito.when(paymentOptionMapperMock.mapToDto(buildPaymentOption())).thenReturn(buildPaymentOptionDTO());

    Pageable pageable = Pageable.ofSize(5);
    Page<DebtPosition> pageDebtPositionsDTO = new PageImpl<>(List.of(debtPosition), pageable, 1);

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of(buildDebtPositionDTO()))
      .size(5L)
      .totalElements(1L)
      .number(0L)
      .totalPages(1L)
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(pageDebtPositionsDTO);

    checkNotNullFields(result);
    reflectionEqualsByName(expectedPagedDebtPositions, result);
  }

  @Test
  void givenNullPagedDebtPositionsThenOk(){
    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of())
      .size(null)
      .totalElements(null)
      .number(null)
      .totalPages(null)
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(null);

    assertEquals(expectedPagedDebtPositions, result);
  }

  @Test
  void givenEmptyPagedDebtPositionsThenOk(){
    Pageable pageable = Pageable.ofSize(5);
    Page<DebtPosition> pageDebtPositionsDTO = new PageImpl<>(List.of(), pageable, 1);

    PagedDebtPositions expectedPagedDebtPositions = PagedDebtPositions.builder()
      .content(List.of())
      .size(5L)
      .totalElements(1L)
      .number(0L)
      .totalPages(1L)
      .build();

    PagedDebtPositions result = debtPositionMapper.mapToPagedDebtPositions(pageDebtPositionsDTO);

    assertEquals(expectedPagedDebtPositions, result);
  }
}
