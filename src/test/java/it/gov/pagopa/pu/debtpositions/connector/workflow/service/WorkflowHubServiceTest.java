package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowHubApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowHubServiceTest {

  @Mock
  private WorkflowHubApiClient workflowHubApiClientMock;

  private WorkflowHubService workflowHubService;

  @BeforeEach
  void init() {
    workflowHubService = new WorkflowHubServiceImpl(
      workflowHubApiClientMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowHubApiClientMock
    );
  }

  @Test
  void whenSyncDebtPositionThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";

    Mockito.when(workflowHubApiClientMock.waitWorkflowCompletion(accessToken, "workflowId", 1, 1))
      .thenReturn("COMPLETED");

    // When
    String result = workflowHubService.waitWorkflowCompletion(accessToken, "workflowId", 1, 1);

    // Then
    Assertions.assertSame("COMPLETED", result);
  }
}
