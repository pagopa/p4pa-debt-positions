package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorflowApiClientTest {
  @Mock
  private WorkflowApisHolder workflowApisHolderMock;
  @Mock
  private DebtPositionApi debtPositionApiMock;

  private WorkflowApiClient workflowApiClient;

  @BeforeEach
  void setUp() {
    workflowApiClient = new WorkflowApiClient(workflowApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApisHolderMock
    );
  }

  @Test
  void whenHandleDpSyncThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    Boolean massive = true;
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.syncDebtPosition(Mockito.same(debtPositionDTO), Mockito.same(massive), Mockito.same(paymentEventType)))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.syncDebtPosition(debtPositionDTO, massive, paymentEventType, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

}
