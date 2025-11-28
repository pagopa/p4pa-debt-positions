package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.co.jemos.podam.api.PodamFactory;

import static org.junit.jupiter.api.Assertions.*;

class DebtorDebtPositionMapperTest {

  private final DebtorDebtPositionMapper mapper =  Mappers.getMapper(DebtorDebtPositionMapper.class);

  private final PodamFactory podam = TestUtils.getPodamFactory();

  @Test
  void givenValidDebtPositionAndTypeOrgWhenMapThenReturnDTO() {
    // given
    DebtPosition debtPosition = podam.manufacturePojo(DebtPosition.class);
    debtPosition.setDebtPositionId(100L);
    debtPosition.setDescription("DEBT_POS_DESC");

    DebtPositionTypeOrg typeOrg = new DebtPositionTypeOrg();
    typeOrg.setDescription("TYPE_ORG_DESCRIPTION");

    // when
    DebtorDebtPositionDTO result = mapper.map(debtPosition, typeOrg);

    // then
    assertNotNull(result);
    assertEquals(100L, result.getDebtPositionId());
    assertEquals("DEBT_POS_DESC", result.getDebtPositionDescription());
    assertEquals("TYPE_ORG_DESCRIPTION", result.getDebtPositionTypeOrgDescription());
    assertEquals(debtPosition.getStatus(), result.getStatus());
    assertEquals(debtPosition.getDebtPositionOrigin(), result.getDebtPositionOrigin());
    assertEquals(debtPosition.getOrganizationId(), result.getOrganizationId());

    TestUtils.checkNotNullFields(result);
  }
}
