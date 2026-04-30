package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeFaker.buildDebtPositionType;

@ExtendWith(MockitoExtension.class)
class TaxonomyCodeConstraintValidatorTest {

  @Mock
  private TaxonomyValidatorService serviceMock;

  @InjectMocks
  private TaxonomyCodeConstraintValidator validator;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(serviceMock);
  }

  @Test
  void givenValidTaxonomyCodeWhenIsValidThenReturnTrue() {
    //Given
    Mockito.when(serviceMock.isTaxonomyCodeValid(Mockito.anyString(), Mockito.anyString()))
      .thenReturn(true);
    //When
    boolean actualResult = validator.isValid(buildDebtPositionType(), null);
    //Then
    Assertions.assertTrue(actualResult);
  }

  @Test
  void givenInvalidTaxonomyCodeWhenIsValidThenThrowInvalidValueException() {
    //Given
    InvalidValueException expectedException = new InvalidValueException("CODE", "Error");
    Mockito.when(serviceMock.isTaxonomyCodeValid(Mockito.anyString(), Mockito.anyString()))
      .thenThrow(expectedException);

    DebtPositionType debtPositionType = buildDebtPositionType();

    //When
    InvalidValueException resultException = Assertions.assertThrows(
      InvalidValueException.class,
      () -> validator.isValid(debtPositionType, null));

    // Then
    Assertions.assertSame(expectedException, resultException);
  }

}
