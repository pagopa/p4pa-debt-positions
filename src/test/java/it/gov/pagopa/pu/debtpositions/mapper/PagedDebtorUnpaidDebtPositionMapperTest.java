package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.BasePaymentOption;
import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtorUnpaidDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.co.jemos.podam.api.PodamFactory;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PagedDebtorUnpaidDebtPositionMapperTest {

  @Mock
  private DataCipherService dataCipherServiceMock;

  private final PagedDebtorUnpaidDebtPositionMapper mapper = Mappers.getMapper(PagedDebtorUnpaidDebtPositionMapper.class);

  private final PodamFactory podam = TestUtils.getPodamFactory();


  @Test
  void givenValidPageAndMapWhenMapThenReturnValidDTO() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionId(10L);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_DESCRIPTION");

    Map<Long, DebtPositionTypeOrg> map = Map.of(10L, typeOrg);
    String debtorFiscalCode = "debtorFiscalCode";

    Page<DebtPosition> page =
      new PageImpl<>(List.of(dp), PageRequest.of(0, 1), 1);

    // when
    PagedDebtorUnpaidDebtPositionDTO result =
      mapper.map(page, map, debtorFiscalCode, dataCipherServiceMock);

    // then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());

    DebtorDebtPositionDTO dto = result.getContent().getFirst();
    assertEquals(10L, dto.getDebtPositionId());
    assertEquals("TYPE_DESCRIPTION", dto.getDebtPositionTypeOrgDescription());

    TestUtils.checkNotNullFields(result);
    result.getContent().forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenEmptyPageWhenMapThenReturnEmptyContent() {
    // given
    Page<DebtPosition> emptyPage = new PageImpl<>(List.of());

    // when
    PagedDebtorUnpaidDebtPositionDTO result =
      mapper.map(emptyPage, Map.of(), null, dataCipherServiceMock);

    // then
    assertNotNull(result);
    assertNotNull(result.getContent());
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  void givenListOfDebtPositionsWhenMapThenAllAreConverted() {
    // given
    DebtPosition dp1 = podam.manufacturePojo(DebtPosition.class);
    dp1.setDebtPositionId(1L);

    DebtPosition dp2 = podam.manufacturePojo(DebtPosition.class);
    dp2.setDebtPositionId(2L);

    DebtPositionTypeOrg t1 = new DebtPositionTypeOrg();
    t1.setDescription("DESC1");

    DebtPositionTypeOrg t2 = new DebtPositionTypeOrg();
    t2.setDescription("DESC2");

    Map<Long, DebtPositionTypeOrg> typeMap = Map.of(
      1L, t1,
      2L, t2
    );

    // when
    List<DebtorDebtPositionDTO> result =
      mapper.map(List.of(dp1, dp2), typeMap, "fc", dataCipherServiceMock);

    // then
    assertEquals(2, result.size());
    assertEquals("DESC1", result.get(0).getDebtPositionTypeOrgDescription());
    assertEquals("DESC2", result.get(1).getDebtPositionTypeOrgDescription());

    result.forEach(TestUtils::checkNotNullFields);
  }

  @Test
  void givenTwoPaymentOptionsWithDifferentDebtorsWhenMapThenOnlyCorrectOneIsKept() {
    // given
    byte[] correctHash = {1, 2, 3};
    byte[] wrongHash = {9, 9, 9};

    String debtorFiscalCode = "DEBTOR_FC";

    Mockito.when(dataCipherServiceMock.hash(debtorFiscalCode))
      .thenReturn(correctHash);

    InstallmentNoPII correctInstallment = InstallmentNoPII.builder()
      .status(InstallmentStatus.UNPAID)
      .debtorFiscalCodeHash(correctHash)
      .dueDate(LocalDate.now())
      .build();

    InstallmentNoPII wrongInstallment = InstallmentNoPII.builder()
      .status(InstallmentStatus.UNPAID)
      .debtorFiscalCodeHash(wrongHash)
      .dueDate(LocalDate.now().plusDays(1))
      .build();

    PaymentOption validPo = PaymentOption.builder()
      .paymentOptionIndex(1)
      .status(PaymentOptionStatus.UNPAID)
      .installments(new TreeSet<>(Set.of(correctInstallment)))
      .build();

    PaymentOption invalidPo = PaymentOption.builder()
      .paymentOptionIndex(2)
      .status(PaymentOptionStatus.UNPAID)
      .installments(new TreeSet<>(Set.of(wrongInstallment)))
      .build();

    DebtPosition dp = DebtPosition.builder()
      .debtPositionId(1L)
      .paymentOptions(new TreeSet<>(Set.of(validPo, invalidPo)))
      .build();

    Page<DebtPosition> page = new PageImpl<>(List.of(dp));

    // when
    PagedDebtorUnpaidDebtPositionDTO result =
      mapper.map(page, Map.of(), debtorFiscalCode, dataCipherServiceMock);

    // then
    DebtorDebtPositionDTO dto = result.getContent().getFirst();

    assertEquals(1, result.getContent().getFirst().getPaymentOptions().size());

    BasePaymentOption remainingPo = dto.getPaymentOptions().getFirst();

    assertEquals(
      1,
      remainingPo.getInstallments().size());

    Collection<InstallmentNoPII> installments = (Collection<InstallmentNoPII>) remainingPo.getInstallments();

    assertArrayEquals(
      correctHash,
      installments.stream().findFirst().get().getDebtorFiscalCodeHash()
    );

  }

  @Test
  void givenNullMapWhenMapThenDescriptionsAreNull() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionId(100L);

    Page<DebtPosition> page = new PageImpl<>(List.of(dp));

    // when
    PagedDebtorUnpaidDebtPositionDTO result = mapper.map(page, null, null, dataCipherServiceMock);

    // then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertNull(
      result.getContent().getFirst().getDebtPositionTypeOrgDescription()
    );

    result.getContent().forEach(
      dpr -> TestUtils.checkNotNullFields(dpr, "debtPositionTypeOrgDescription")
    );
  }
}


