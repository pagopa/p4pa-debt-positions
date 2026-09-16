package it.gov.pagopa.pu.debtpositions.connector.migration.service;

import it.gov.pagopa.pu.debtpositions.connector.auth.AuthnService;
import it.gov.pagopa.pu.debtpositions.connector.migration.client.DebtPositionTypeOrgOperatorsClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgOperatorsServiceTest {
  @Mock
  private AuthnService authnServiceMock;

  @Mock
  private DebtPositionTypeOrgOperatorsClient debtPositionTypeOrgOperatorsClientMock;

  private DebtPositionTypeOrgOperatorsService debtPositionTypeOrgOperatorsService;

  @BeforeEach
  void init() {
    debtPositionTypeOrgOperatorsService = new DebtPositionTypeOrgOperatorsServiceImpl(authnServiceMock, debtPositionTypeOrgOperatorsClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      authnServiceMock,
      debtPositionTypeOrgOperatorsClientMock
    );
  }

  @Test
  void whenGetUnconsumedDebtPositionTypeOrgIdsThenOk() {
    // Given
    Long organizationId = 1L;
    String fiscalCode = "fiscalCode";
    String orgIpaCode = "orgIpaCode";
    String accessToken = "ACCESSTOKEN";
    List<Long> expectedResult = List.of(1L, 2L);

    when(authnServiceMock.getAccessToken(orgIpaCode))
      .thenReturn(accessToken);
    when(debtPositionTypeOrgOperatorsClientMock.getUnconsumedDebtPositionTypeOrgIds(organizationId,fiscalCode,accessToken))
      .thenReturn(expectedResult);

    // When
    List<Long> result = debtPositionTypeOrgOperatorsService.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode, orgIpaCode);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void whenConsumeDebtPositionTypeOrgOperatorsThenOk() {
    // Given
    Long organizationId = 1L;
    String fiscalCode = "fiscalCode";
    String orgIpaCode = "orgIpaCode";
    String accessToken = "ACCESSTOKEN";
    List<Long> debtPositionTypeOrgIds = List.of(1L, 2L);

    when(authnServiceMock.getAccessToken(orgIpaCode))
      .thenReturn(accessToken);
    doNothing().when(debtPositionTypeOrgOperatorsClientMock).consumeDebtPositionTypeOrgOperators(organizationId,debtPositionTypeOrgIds,fiscalCode,accessToken);

    // When
    Assertions.assertDoesNotThrow(()->debtPositionTypeOrgOperatorsService.consumeDebtPositionTypeOrgOperators(organizationId, debtPositionTypeOrgIds, fiscalCode, orgIpaCode));
  }
}
