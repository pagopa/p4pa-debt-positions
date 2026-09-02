package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeFaker.buildDebtPositionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryResolverServiceTest {

  @Mock
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;

  @Mock
  private TaxonomyValidatorService taxonomyValidatorServiceMock;

  private CategoryResolverService categoryResolverService;

  @BeforeEach
  void setUp() {
    categoryResolverService = new CategoryResolverService(debtPositionTypeRepositoryMock, taxonomyValidatorServiceMock);
  }

  @Test
  void whenValidLegacyPaymentMetadataWhenFetchCategoryThenSuccess() {
    String legacyPaymentMetadata = "9/001122233/xxx";
    Long debtPositionTypeId = 1L;
    String orgTypeCode = "00";

    when(taxonomyValidatorServiceMock.isTaxonomyCodeValid("9/001122233/", orgTypeCode)).thenReturn(true);

    String result = categoryResolverService.resolveCategory(legacyPaymentMetadata, debtPositionTypeId, orgTypeCode, DebtPositionOrigin.ORDINARY);

    assertEquals("9/001122233/", result);

    verify(debtPositionTypeRepositoryMock, times(0)).findById(debtPositionTypeId);
  }

  @Test
  void whenTaxonomyCategoryInvalidWhenFetchCategoryThenSuccess() {
    String legacyPaymentMetadata = "9/001122233/xxx";
    Long debtPositionTypeId = 1L;
    String orgTypeCode = "00";

    when(taxonomyValidatorServiceMock.isTaxonomyCodeValid("9/001122233/", orgTypeCode)).thenReturn(false);
    when(debtPositionTypeRepositoryMock.findById(debtPositionTypeId)).thenReturn(Optional.of(buildDebtPositionType()));

    String result = categoryResolverService.resolveCategory(legacyPaymentMetadata, debtPositionTypeId, orgTypeCode, DebtPositionOrigin.ORDINARY);

    assertEquals("9/001122233/", result);
  }

  @Test
  void whenInvalidLegacyPaymentMetadataWhenFetchCategoryThenGetFromDP() {
    String legacyPaymentMetadata = "001122233/xxx";
    Long debtPositionTypeId = 1L;
    String orgTypeCode = "00";

    when(debtPositionTypeRepositoryMock.findById(debtPositionTypeId)).thenReturn(Optional.of(buildDebtPositionType()));

    String result = categoryResolverService.resolveCategory(legacyPaymentMetadata, debtPositionTypeId, orgTypeCode, DebtPositionOrigin.ORDINARY);

    assertEquals("9/001122233/", result);
  }

  @Test
  void whenNullLegacyPaymentMetadataWhenFetchCategoryThenSuccess() {
    Long debtPositionTypeId = 1L;
    String orgTypeCode = "00";

    when(debtPositionTypeRepositoryMock.findById(debtPositionTypeId)).thenReturn(Optional.of(buildDebtPositionType()));

    String result = categoryResolverService.resolveCategory(null, debtPositionTypeId, orgTypeCode, DebtPositionOrigin.ORDINARY);

    assertEquals("9/001122233/", result);
  }

  @Test
  void whenInvalidDebtPositionTypeIdWhenFetchCategoryThenException() {
    Long debtPositionTypeId = 1L;

    when(debtPositionTypeRepositoryMock.findById(debtPositionTypeId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> categoryResolverService.resolveCategory(null, debtPositionTypeId, null, DebtPositionOrigin.ORDINARY));

    assertEquals("DEBT_POSITION_TYPE_NOT_FOUND",exception.getCode());
    assertEquals("The debt position type with id 1 is not found", exception.getMessage());
  }
}
