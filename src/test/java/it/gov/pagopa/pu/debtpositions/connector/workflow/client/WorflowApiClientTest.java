package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.controller.generated.DebtPositionApi;
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
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.handleDpSync(debtPositionDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.handleDpSync(debtPositionDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpSyncAcaThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpSyncAca(debtPositionDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpSyncAca(debtPositionDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpSyncGpdPreloadThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpSyncGpdPreload(debtPositionDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpSyncGpdPreload(debtPositionDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpSyncAcaGpdPreloadThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpSyncAcaGpdPreload(debtPositionDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpSyncAcaGpdPreload(debtPositionDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }

  @Test
  void whenAlignDpGPDThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");

    Mockito.when(workflowApisHolderMock.getDebtPositionApi(accessToken))
      .thenReturn(debtPositionApiMock);
    Mockito.when(debtPositionApiMock.alignDpGPD(debtPositionDTO))
      .thenReturn(new WorkflowCreatedDTO("1"));

    // When
    WorkflowCreatedDTO result = workflowApiClient.alignDpGPD(debtPositionDTO, accessToken);

    // Then
    Assertions.assertSame(expectedResult.getWorkflowId(), result.getWorkflowId());
  }
}
