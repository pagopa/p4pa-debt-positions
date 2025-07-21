package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
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
    Boolean result = balanceService.isValidBalance(balance, accessToken);

    // Then
    Assertions.assertEquals(Boolean.TRUE, result);
  }

  @Test
  void givenBalanceWhenGetByAssessmentThenBalance() {
    // Given
    String balance = "balance";
    Long orgId = 1L;
    String debtPositionTypeOrgCode = "CODE";
    String accessToken = "ACCESSTOKEN";

    Mockito.when(balanceClientMock.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode, accessToken))
      .thenReturn(balance);

    // When
    String result = balanceService.getBalanceByAssessmentRegistry(orgId, debtPositionTypeOrgCode, accessToken);

    // Then
    Assertions.assertEquals(balance, result);
  }

  @Test
  void givenBalanceWithPlaceholderWhenCalculateAmountThenBalanceResolved() {
    // Given
    CalculateAmountBalanceRequest request = CalculateAmountBalanceRequest.builder()
      .balance("<accertamento><importo>TOTALE</importo></accertamento>")
      .amountCents(100L)
      .remittanceInformation("info")
      .build();
    String accessToken = "ACCESSTOKEN";
    String balanceResolved = "<accertamento><importo>1.00</importo></accertamento>";

    Mockito.when(balanceClientMock.calculateAmountBalance(request, accessToken))
      .thenReturn(balanceResolved);

    // When
    String result = balanceService.calculateAmountBalance(request, accessToken);

    // Then
    Assertions.assertEquals(balanceResolved, result);
  }
}
