package it.gov.pagopa.pu.debtpositions.connector.classification.client;

import it.gov.pagopa.pu.classification.controller.generated.BalanceApi;
import it.gov.pagopa.pu.classification.dto.generated.ValidateBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.config.ClassificationApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceClientTest {
  @Mock
  private ClassificationApisHolder classificationApisHolder;
  @Mock
  private BalanceApi balanceApiMock;

  private BalanceClient balanceClient;

  @BeforeEach
  void setUp() {
    balanceClient = new BalanceClient(classificationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      classificationApisHolder
    );
  }

  @Test
  void givenValidBalanceWhenValidateThenTrue() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    String balance = "balance";

    Mockito.when(classificationApisHolder.getBalanceApi(accessToken))
      .thenReturn(balanceApiMock);
    Mockito.when(balanceApiMock.validateBalance(ValidateBalanceRequest.builder().balance(balance).build()))
      .thenReturn(Boolean.TRUE);

    // When
    Boolean result = balanceClient.validateBalance(balance, accessToken);

    // Then
    Assertions.assertSame(Boolean.TRUE, result);
  }
}
