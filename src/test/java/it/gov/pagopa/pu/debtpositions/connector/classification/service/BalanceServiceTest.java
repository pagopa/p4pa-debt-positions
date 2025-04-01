package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.debtpositions.connector.classification.client.BalanceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

  @Mock
  private BalanceClient balanceClientMock;

  private BalanceService balanceService;

  @BeforeEach
  void init() {
    balanceService = new BalanceServiceImpl(balanceClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(balanceClientMock);
  }

  @Test
  void givenBalanceValidWhenValidateThenTrue() {
    // Given
    String balance = "balance";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(balanceClientMock.validateBalance(balance, accessToken))
      .thenReturn(Boolean.TRUE);

    // When
    Boolean result = balanceService.validateBalance(balance, accessToken);

    // Then
    Assertions.assertEquals(Boolean.TRUE, result);
  }
}
