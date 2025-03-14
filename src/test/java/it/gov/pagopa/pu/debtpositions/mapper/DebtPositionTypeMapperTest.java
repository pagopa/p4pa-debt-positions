package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeMapperTest {
  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @InjectMocks
  private DebtPositionTypeMapper debtPositionTypeMapper;

  @Test
  void testMapDebtPositionTypeDetailDTO() {
    //given
    DebtPositionType debtPositionType = podamFactory.manufacturePojo(DebtPositionType.class);
    Taxonomy taxonomy = podamFactory.manufacturePojo(Taxonomy.class);

    //when
    DebtPositionTypeDetailDTO result = debtPositionTypeMapper.mapToDebtPositionTypeDetailDTO(debtPositionType, taxonomy);

    //verify
    Assertions.assertNotNull(result);
    TestUtils.checkNotNullFields(result);
    Assertions.assertEquals(debtPositionType.getDebtPositionTypeId(), result.getDebtPositionTypeId());
    Assertions.assertEquals(debtPositionType.getCode(), result.getCode());
    Assertions.assertEquals(debtPositionType.getDescription(), result.getDescription());
    Assertions.assertEquals(debtPositionType.getTaxonomyCode(), result.getTaxonomyCode());
    Assertions.assertEquals(debtPositionType.isFlagAnonymousFiscalCode(), result.getFlagAnonymousFiscalCode());
    Assertions.assertEquals(debtPositionType.isFlagMandatoryDueDate(), result.getFlagMandatoryDueDate());
    Assertions.assertEquals(debtPositionType.isFlagNotifyIo(), result.getFlagNotifyIo());
    Assertions.assertEquals(taxonomy.getOrganizationTypeDescription(), result.getOrganizationTypeDescription());
    Assertions.assertEquals(taxonomy.getMacroAreaName(), result.getMacroAreaName());
    Assertions.assertEquals(taxonomy.getServiceType(), result.getServiceType());
    Assertions.assertEquals(taxonomy.getCollectionReason(), result.getCollectionReason());
  }

  @Test
  void givenNullDebtPositionTypeThenReturnNull() {
    //given
    Taxonomy taxonomy = podamFactory.manufacturePojo(Taxonomy.class);

    //verify
    Assertions.assertNull(debtPositionTypeMapper.mapToDebtPositionTypeDetailDTO(null, taxonomy));
  }

  @Test
  void givenNullTaxonomyThenReturnNull() {
    //given
    DebtPositionType debtPositionType = podamFactory.manufacturePojo(DebtPositionType.class);

    //verify
    Assertions.assertNull(debtPositionTypeMapper.mapToDebtPositionTypeDetailDTO(debtPositionType, null));
  }
}
