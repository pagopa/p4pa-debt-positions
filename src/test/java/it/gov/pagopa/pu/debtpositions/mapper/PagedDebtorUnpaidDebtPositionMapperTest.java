package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedDebtorUnpaidDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PagedDebtorUnpaidDebtPositionMapperTest {

  private final PagedDebtorUnpaidDebtPositionMapper mapper =
    Mappers.getMapper(PagedDebtorUnpaidDebtPositionMapper.class);

  private final PodamFactory podam = TestUtils.getPodamFactory();

  @Test
  void givenValidPageAndMapWhenMapThenReturnValidDTO() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionId(10L);

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_DESCRIPTION");

    Map<Long, DebtPositionTypeOrg> map = Map.of(10L, typeOrg);

    Page<DebtPosition> page =
      new PageImpl<>(List.of(dp), PageRequest.of(0, 1), 1);

    // when
    PagedDebtorUnpaidDebtPositionDTO result = mapper.map(page, map);

    // then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());

    DebtorDebtPositionDTO dto = result.getContent().get(0);
    assertEquals(dp.getDebtPositionId(), dto.getDebtPositionId());
    assertEquals("TYPE_DESCRIPTION", dto.getDebtPositionTypeOrgDescription());
    TestUtils.checkNotNullFields(result);
  }

  @Test
  void givenEmptyPageWhenMapThenReturnEmptyContent() {
    // given
    Page<DebtPosition> emptyPage = new PageImpl<>(List.of());

    // when
    PagedDebtorUnpaidDebtPositionDTO result = mapper.map(emptyPage, null);

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

    List<DebtPosition> list = List.of(dp1, dp2);

    // when
    List<DebtorDebtPositionDTO> result = mapper.map(list, typeMap);

    // then
    assertEquals(2, result.size());
    assertEquals("DESC1", result.get(0).getDebtPositionTypeOrgDescription());
    assertEquals("DESC2", result.get(1).getDebtPositionTypeOrgDescription());
    result.forEach(
      TestUtils::checkNotNullFields
    );

  }

  @Test
  void givenNullMapWhenMapThenDescriptionsAreNull() {
    // given
    DebtPosition dp = podam.manufacturePojo(DebtPosition.class);
    dp.setDebtPositionId(100L);

    Page<DebtPosition> page = new PageImpl<>(List.of(dp));

    // when
    PagedDebtorUnpaidDebtPositionDTO result = mapper.map(page, null);

    // then
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertNull(result.getContent().getFirst().getDebtPositionTypeOrgDescription());

    result.getContent().forEach(
     dpr -> TestUtils.checkNotNullFields(dpr,"debtPositionTypeOrgDescription")
    );
  }
}


