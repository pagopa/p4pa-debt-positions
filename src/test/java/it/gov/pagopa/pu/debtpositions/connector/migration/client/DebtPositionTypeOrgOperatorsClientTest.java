package it.gov.pagopa.pu.debtpositions.connector.migration.client;

import it.gov.pagopa.pu.debtpositions.connector.migration.config.MigrationApisHolder;
import it.gov.pagopa.pu.migration.client.generated.DebtPositionTypeOrgOperatorsApi;
import it.gov.pagopa.pu.migration.dto.generated.ConsumeDebtPositionTypeOrgOperatorsDTO;
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
class DebtPositionTypeOrgOperatorsClientTest {
  @Mock
  private MigrationApisHolder migrationApisHolderMock;
  @Mock
  private DebtPositionTypeOrgOperatorsApi debtPositionTypeOrgOperatorsApiMock;

  private DebtPositionTypeOrgOperatorsClient debtPositionTypeOrgOperatorsClient;

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgOperatorsClient = new DebtPositionTypeOrgOperatorsClient(migrationApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      migrationApisHolderMock,
      debtPositionTypeOrgOperatorsApiMock
    );
  }

  @Test
  void whenGetUnconsumedDebtPositionTypeOrgIdsThenOk() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;
    String fiscalCode = "fiscalCode";
    List<Long> expectedResult = List.of(1L, 2L, 3L);

    when(migrationApisHolderMock.getDebtPositionTypeOrgOperatorsApi(accessToken))
      .thenReturn(debtPositionTypeOrgOperatorsApiMock);
    when(debtPositionTypeOrgOperatorsApiMock.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode))
      .thenReturn(expectedResult);

    // When
    List<Long> result = debtPositionTypeOrgOperatorsClient.getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenConsumeDebtPositionTypeOrgOperatorsThenOk() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;
    String fiscalCode = "fiscalCode";
    List<Long> debtPositionTypeOrgIds = List.of(1L, 2L, 3L);
    ConsumeDebtPositionTypeOrgOperatorsDTO consumeDebtPositionTypeOrgOperatorsDTO = ConsumeDebtPositionTypeOrgOperatorsDTO.builder().debtPositionTypeOrgIds(debtPositionTypeOrgIds).fiscalCode(fiscalCode).build();

    when(migrationApisHolderMock.getDebtPositionTypeOrgOperatorsApi(accessToken))
      .thenReturn(debtPositionTypeOrgOperatorsApiMock);
    doNothing().when(debtPositionTypeOrgOperatorsApiMock).consumeDebtPositionTypeOrgOperators(organizationId, consumeDebtPositionTypeOrgOperatorsDTO);

    // When
    Assertions.assertDoesNotThrow(()->debtPositionTypeOrgOperatorsClient.consumeDebtPositionTypeOrgOperators(organizationId, debtPositionTypeOrgIds, fiscalCode, accessToken));
  }
}
