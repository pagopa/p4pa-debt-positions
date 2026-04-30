package it.gov.pagopa.pu.debtpositions.connector.classification.client;

import it.gov.pagopa.pu.classification.controller.generated.BalanceApi;
import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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
    Long amountCents = 100L;

    Mockito.when(classificationApisHolder.getBalanceApi(accessToken))
      .thenReturn(balanceApiMock);
    Mockito.when(balanceApiMock.validateBalance(ValidateBalanceRequest.builder().balance(balance).amountCents(amountCents).build()))
      .thenReturn(Boolean.TRUE);

    // When
    Boolean result = balanceClient.validateBalance(balance, amountCents, accessToken);

    // Then
    Assertions.assertSame(Boolean.TRUE, result);
  }

  @Test
  void givenBalanceWhenGetByAssessmentThenTrue() {
    // Given
    String balance = "balance";
    Long orgId = 1L;
    String debtPositionTypeOrgCode = "CODE";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(classificationApisHolder.getBalanceApi(accessToken))
      .thenReturn(balanceApiMock);

    Mockito.when(balanceApiMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode))
      .thenReturn(balance);

    // When
    String result = balanceClient.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode, accessToken);

    // Then
    Assertions.assertEquals(balance, result);
  }

  @Test
  void givenBalanceNotFoundWhenGetByAssessmentThenNull() {
    // Given
    Long orgId = 1L;
    String debtPositionTypeOrgCode = "CODE";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(classificationApisHolder.getBalanceApi(accessToken))
      .thenReturn(balanceApiMock);

    Mockito.when(balanceApiMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    String result = balanceClient.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode, accessToken);

    // Then
    Assertions.assertNull(result);
  }

  @Test
  void givenBalanceWhenCalculateAmountThenBalanceResolved() {
    // Given
    CalculateAmountBalanceRequest request = CalculateAmountBalanceRequest.builder()
      .balance("<accertamento><importo>TOTALE</importo></accertamento>")
      .amountCents(100L)
      .remittanceInformation("info")
      .build();
    String accessToken = "ACCESSTOKEN";
    String balanceResolved = "<accertamento><importo>1.00</importo></accertamento>";

    Mockito.when(classificationApisHolder.getBalanceApi(accessToken))
      .thenReturn(balanceApiMock);

    Mockito.when(balanceApiMock.calculateAmountBalance(request)).thenReturn(balanceResolved);

    // When
    String result = balanceClient.calculateAmountBalance(request, accessToken);

    // Then
    Assertions.assertEquals(balanceResolved, result);
  }
}
