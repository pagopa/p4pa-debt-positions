package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.client.generated.WorkflowApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowStatusDTO;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowHubApiClientTest {

  @Mock
  private WorkflowApisHolder workflowApisHolderMock;
  @Mock
  private WorkflowApi workflowApiMock;

  private WorkflowHubApiClient client;

  @BeforeEach
  void setUp() {
    client = new WorkflowHubApiClient(workflowApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApisHolderMock
    );
  }

  @Test
  void whenWaitForWFCompletionThenInvokeWithAccessToken() {
    // Given
    String accessToken = "ACCESSTOKEN";

    when(workflowApisHolderMock.getWorkflowApi(accessToken))
      .thenReturn(workflowApiMock);
    when(workflowApiMock.waitWorkflowCompletion("workflowId", 1, 1))
      .thenReturn(new WorkflowStatusDTO().status("COMPLETED"));

    // When
    String result = client.waitWorkflowCompletion(accessToken, "workflowId", 1, 1);

    // Then
    Assertions.assertSame("COMPLETED", result);
  }

  @Test
  void whenWaitForWFCompletionWithWFNotFoundThenException() {
    // Given
    String accessToken = "ACCESSTOKEN";

    when(workflowApisHolderMock.getWorkflowApi(accessToken))
      .thenReturn(workflowApiMock);
    when(workflowApiMock.waitWorkflowCompletion("workflowId", 1, 1))
      .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "NotFound", null, null, null));

    // When
    String result = client.waitWorkflowCompletion(accessToken, "workflowId", 1, 1);

    // Then
    Assertions.assertNull(result);
  }
}
