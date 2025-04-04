package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceConstraintValidatorTest {

  @Mock
  private BalanceService balanceServiceMock;

  private BalanceConstraintValidator validator;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
    validator = new BalanceConstraintValidator(balanceServiceMock);
  }

  @AfterEach
  void clear(){
    SecurityUtilsTest.clearSecurityContext();
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(balanceServiceMock);
  }

  @Test
  void givenBalanceNullWhenValidatorHandlerThenSuccess(){
    Assertions.assertTrue(() -> validator.isValid(null, null));
  }

  @Test
  void givenBalanceNotValidWhenIsValidThenTrue(){
    // Given
    String balance = "balance";
    Mockito.when(balanceServiceMock.isValidBalance(balance, accessToken))
      .thenReturn(true);

    // When
    boolean result = validator.isValid(balance, null);

    // Then
    Assertions.assertTrue(result);
  }

  @Test
  void givenBalanceNotValidWhenIsValidThenFalse(){
    // Given
    String balance = "balance";
    Mockito.when(balanceServiceMock.isValidBalance(balance, accessToken))
      .thenReturn(false);

    // When
    boolean result = validator.isValid(balance, null);

    // Then
    Assertions.assertFalse(result);
  }
}
