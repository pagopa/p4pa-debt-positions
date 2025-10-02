package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.service.CategoryValidatorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxonomyCodeConstraintValidatorTest {

  @Mock
  private CategoryValidatorService serviceMock;

  @InjectMocks
  private TaxonomyCodeConstraintValidator validator;

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(serviceMock);
  }

  @Test
  void givenValidTaxonomyCodeWhenIsValidThenReturnTrue() {
    //Given
    Mockito.when(serviceMock.isTaxonomyCodeValid(Mockito.anyString()))
      .thenReturn(true);
    //When
    boolean actualResult = validator.isValid("valid_taxonomy_code", null);
    //Then
    Assertions.assertTrue(actualResult);
  }

  @Test
  void givenInvalidTaxonomyCodeWhenIsValidThenThrowInvalidCategoryException() {
    //Given
    Mockito.when(serviceMock.isTaxonomyCodeValid(Mockito.anyString()))
      .thenThrow(new InvalidValueException("Error"));
    //When, then
    InvalidValueException actualException = Assertions.assertThrows(
      InvalidValueException.class,
      () -> validator.isValid("invalid_taxonomy_code", null)
    );
    Assertions.assertEquals("Error", actualException.getMessage());
  }

}
