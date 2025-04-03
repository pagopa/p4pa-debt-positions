package it.gov.pagopa.pu.debtpositions.repository.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgValidatorHandlerTest {

  @Mock
  private BalanceService balanceServiceMock;

  private DebtPositionTypeOrgValidatorHandler handler;

  @BeforeEach
  void init() {
    handler = new DebtPositionTypeOrgValidatorHandler(balanceServiceMock);
  }

  @Test
  void givenBalanceNullWhenValidatorHandlerThenSuccess(){
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance(null);

    Assertions.assertDoesNotThrow(() -> handler.handleBeforeSaveOrCreate(debtPositionTypeOrg));
    Mockito.verify(balanceServiceMock, Mockito.times(0)).isValidBalance(Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void givenBalanceValidWhenValidateThenSuccess(){
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance("balance");

    Mockito.when(balanceServiceMock.isValidBalance("balance", null))
      .thenReturn(Boolean.TRUE);

    Assertions.assertDoesNotThrow(() -> handler.handleBeforeSaveOrCreate(debtPositionTypeOrg));
  }

  @Test
  void givenBalanceNotValidWhenValidateThenSuccess(){
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setBalance("balanceInvalid");

    Mockito.when(balanceServiceMock.isValidBalance("balanceInvalid", null))
      .thenReturn(Boolean.FALSE);

    InvalidValueException exception = Assertions.assertThrows(InvalidValueException.class, () -> handler.handleBeforeSaveOrCreate(debtPositionTypeOrg));
    assertEquals("Balance in debt position type org is not formally valid", exception.getMessage());
  }
}
