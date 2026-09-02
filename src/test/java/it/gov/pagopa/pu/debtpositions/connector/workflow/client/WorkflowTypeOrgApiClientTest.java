package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.exception.common.RestInvokeNotFoundException;
import it.gov.pagopa.pu.workflowhub.client.generated.WorkflowTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowTypeOrgApiClientTest {

  @Mock
  private WorkflowApisHolder workflowApisHolderMock;

  @Mock
  private WorkflowTypeOrgEntityControllerApi workflowTypeOrgEntityControllerApiMock;

  private WorkflowTypeOrgApiClient workflowTypeOrgApiClient;

  @BeforeEach
  void init() {
    workflowTypeOrgApiClient = new WorkflowTypeOrgApiClient(workflowApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowApisHolderMock
    );
  }

  @Test
  void whenFindByIdThenOk() {
    String accessToken = "accessToken";
    String workflowTypeOrgId = "id";
    WorkflowTypeOrg expectedResult = new WorkflowTypeOrg();

    when(workflowApisHolderMock.getWorkflowTypeOrgEntityControllerApi(accessToken))
      .thenReturn(workflowTypeOrgEntityControllerApiMock);
    when(workflowTypeOrgEntityControllerApiMock.crudGetWorkflowtypeorg(workflowTypeOrgId))
      .thenReturn(expectedResult);

    WorkflowTypeOrg result = workflowTypeOrgApiClient.findById(workflowTypeOrgId, accessToken);

    assertSame(expectedResult, result);
  }

  @Test
  void givenNotFoundWhenFindByIdThenNull() {
    String accessToken = "accessToken";
    String workflowTypeOrgId = "id";

    when(workflowApisHolderMock.getWorkflowTypeOrgEntityControllerApi(accessToken))
      .thenReturn(workflowTypeOrgEntityControllerApiMock);
    when(workflowTypeOrgEntityControllerApiMock.crudGetWorkflowtypeorg(workflowTypeOrgId))
      .thenThrow(new RestInvokeNotFoundException("APPNAME", HttpStatus.NOT_FOUND, "ERROR", "ERRORCODE", "ERRORMESSAGE"));

    WorkflowTypeOrg result = workflowTypeOrgApiClient.findById(workflowTypeOrgId, accessToken);

    assertNull(result);
  }

}
