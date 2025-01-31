package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
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
    DebtPositionRequestDTO debtPositionRequestDTO = new DebtPositionRequestDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.handleDpSync(debtPositionRequestDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.handleDpSync(debtPositionRequestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpSyncAcaThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionRequestDTO debtPositionRequestDTO = new DebtPositionRequestDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpSyncAca(debtPositionRequestDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpSyncAca(debtPositionRequestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpSyncGpdPreloadThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionRequestDTO debtPositionRequestDTO = new DebtPositionRequestDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpSyncGpdPreload(debtPositionRequestDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpSyncGpdPreload(debtPositionRequestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpSyncAcaGpdPreloadThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionRequestDTO debtPositionRequestDTO = new DebtPositionRequestDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpSyncAcaGpdPreload(debtPositionRequestDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpSyncAcaGpdPreload(debtPositionRequestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpGPDThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionRequestDTO debtPositionRequestDTO = new DebtPositionRequestDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpGPD(debtPositionRequestDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpGPD(debtPositionRequestDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }
}
