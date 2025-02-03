package it.gov.pagopa.pu.debtpositions.mapper.workflow;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgMapperTest {

  @InjectMocks
  private final DebtPositionTypeOrgMapper mapper = Mappers.getMapper(DebtPositionTypeOrgMapper.class);

  @Test
  void testMapDebtPositionTypeOrg() {
    DebtPositionTypeOrgDTO expected = buildDebtPositionTypeOrgDTO();

    DebtPositionTypeOrgDTO result = mapper.map(buildDebtPositionTypeOrg());

    assertEquals(expected, result);
    checkNotNullFields(result);
  }
}
