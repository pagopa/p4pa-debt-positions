package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.PostalIbanVerifyResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PostalIbanVerifyResponseMapperTest {

  private final PostalIbanVerifyResponseMapper mapper = new PostalIbanVerifyResponseMapper();

  @Test
  void givenSomeIdsWithNullPostalIbanWhenMapThenCorrectBooleans() {
    // given
    List<Long> installmentIds = List.of(1L, 2L, 3L);
    Set<Long> idsWithNull = Set.of(2L);

    // when
    PostalIbanVerifyResponse result = mapper.map(installmentIds, idsWithNull);

    // then
    assertNotNull(result);
    Map<String, Boolean> map = result.getInstallmentPostalIbanCheck();

    assertEquals(3, map.size());
    assertTrue(map.get("1"));   // non è in idsWithNull
    assertFalse(map.get("2"));  // è in idsWithNull
    assertTrue(map.get("3"));
  }

  @Test
  void givenNoNullPostalIbanWhenMapThenAllTrue() {
    // given
    List<Long> installmentIds = List.of(10L, 20L);
    Set<Long> idsWithNull = Set.of();

    // when
    PostalIbanVerifyResponse result = mapper.map(installmentIds, idsWithNull);

    // then
    Map<String, Boolean> map = result.getInstallmentPostalIbanCheck();

    assertTrue(map.get("10"));
    assertTrue(map.get("20"));
  }

  @Test
  void givenAllNullPostalIbanWhenMapThenAllFalse() {
    // given
    List<Long> installmentIds = List.of(5L, 6L);
    Set<Long> idsWithNull = Set.of(5L, 6L);

    // when
    PostalIbanVerifyResponse result = mapper.map(installmentIds, idsWithNull);

    // then
    Map<String, Boolean> map = result.getInstallmentPostalIbanCheck();

    assertFalse(map.get("5"));
    assertFalse(map.get("6"));
  }

  @Test
  void givenEmptyInstallmentIdsWhenMapThenEmptyMap() {
    // given
    List<Long> installmentIds = List.of();
    Set<Long> idsWithNull = Set.of(1L);

    // when
    PostalIbanVerifyResponse result = mapper.map(installmentIds, idsWithNull);

    // then
    assertNotNull(result);
    assertTrue(result.getInstallmentPostalIbanCheck().isEmpty());
  }
}
