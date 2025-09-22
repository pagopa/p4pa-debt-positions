package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeFaker.buildDebtPositionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryResolverServiceTest {

  @Mock
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;

  private CategoryResolverService service;

  @BeforeEach
  void setUp() {
    service = new CategoryResolverService(debtPositionTypeRepositoryMock);
  }

  @Test
  void whenValidLegacyPaymentMetadataWhenFetchCategoryThenSuccess() {
    String legacyPaymentMetadata = "9/001122233/xxx";
    Long debtPositionTypeId = 1L;

    String result = service.resolveCategory(legacyPaymentMetadata, debtPositionTypeId);

    assertEquals("001122233", result);

    verify(debtPositionTypeRepositoryMock, times(0)).findById(debtPositionTypeId);
  }

  @Test
  void whenInvalidLegacyPaymentMetadataWhenFetchCategoryThenException() {
    String legacyPaymentMetadata = "001122233/xxx";
    Long debtPositionTypeId = 1L;

    InvalidValueException exception = assertThrows(InvalidValueException.class, () -> service.resolveCategory(legacyPaymentMetadata, debtPositionTypeId));

    assertEquals("The legacy payment metadata [001122233/xxx] is not valid to extract taxonomy code", exception.getMessage());
  }

  @Test
  void whenNullLegacyPaymentMetadataWhenFetchCategoryThenSuccess() {
    Long debtPositionTypeId = 1L;

    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeId)).thenReturn(Optional.of(buildDebtPositionType()));

    String result = service.resolveCategory(null, debtPositionTypeId);

    assertEquals("001122233", result);
  }

  @Test
  void whenInvalidDebtPositionTypeIdWhenFetchCategoryThenException() {
    Long debtPositionTypeId = 1L;

    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> service.resolveCategory(null, debtPositionTypeId));

    assertEquals("The debt position type with id 1 is not found", exception.getMessage());
  }
}
