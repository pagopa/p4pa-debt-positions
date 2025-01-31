package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowApiClient;
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
class WorkflowServiceTest {

  @Mock
  private WorkflowApiClient workflowApiClientMock;

  private WorkflowService workflowService;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    workflowService = new WorkflowServiceImpl(
      workflowApiClientMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApiClientMock
    );
  }

  @Test
  void givenValidDebtPositionRequestDTOWhenHandleDpSyncThenEmpty() {
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");
    Mockito.when(workflowApiClientMock.handleDpSync(new DebtPositionRequestDTO(), accessToken))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.handleDpSync(new DebtPositionRequestDTO(), accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenValidDebtPositionRequestDTOWhenAlignDpSyncAcaThenEmpty(){
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");
    Mockito.when(workflowApiClientMock.alignDpSyncAca(new DebtPositionRequestDTO(), accessToken))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.alignDpSyncAca(new DebtPositionRequestDTO(), accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenValidDebtPositionRequestDTOWhenAlignDpSyncGpdPreloadThenEmpty(){
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");
    Mockito.when(workflowApiClientMock.alignDpSyncGpdPreload(new DebtPositionRequestDTO(), accessToken))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.alignDpSyncGpdPreload(new DebtPositionRequestDTO(), accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenValidDebtPositionRequestDTOWhenAlignDpSyncAcaGpdPreloadThenEmpty(){
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");
    Mockito.when(workflowApiClientMock.alignDpSyncAcaGpdPreload(new DebtPositionRequestDTO(), accessToken))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.alignDpSyncAcaGpdPreload(new DebtPositionRequestDTO(), accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenValidDebtPositionRequestDTOWhenAlignDpGPDThenEmpty(){
    // Given
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1");
    Mockito.when(workflowApiClientMock.alignDpGPD(new DebtPositionRequestDTO(), accessToken))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.alignDpGPD(new DebtPositionRequestDTO(), accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
