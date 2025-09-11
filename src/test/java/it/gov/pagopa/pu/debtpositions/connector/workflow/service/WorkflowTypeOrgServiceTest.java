package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowTypeOrgApiClient;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowTypeOrgServiceTest {

  @Mock
  private WorkflowTypeOrgApiClient workflowTypeOrgApiClientMock;

  private WorkflowTypeOrgService workflowTypeOrgService;

  @BeforeEach
  void init() {
    workflowTypeOrgService = new WorkflowTypeOrgServiceImpl(workflowTypeOrgApiClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      workflowTypeOrgApiClientMock
    );
  }

  @Test
  void whenGetByIdThenOk() {
    String accessToken = "accessToken";
    String workflowTypeOrgId = "id";
    WorkflowTypeOrg expectedResult = new WorkflowTypeOrg();

    Mockito.when(workflowTypeOrgApiClientMock.findById(workflowTypeOrgId, accessToken))
      .thenReturn(expectedResult);

    Optional<WorkflowTypeOrg> result = workflowTypeOrgService.getById(workflowTypeOrgId, accessToken);

    assertTrue(result.isPresent());
    assertSame(expectedResult, result.get());
  }

  @Test
  void givenNullWorkflowTypeOrgWhenGetByIdThenEmpty() {
    String accessToken = "accessToken";
    String workflowTypeOrgId = "id";

    Mockito.when(workflowTypeOrgApiClientMock.findById(workflowTypeOrgId, accessToken))
      .thenReturn(null);

    Optional<WorkflowTypeOrg> result = workflowTypeOrgService.getById(workflowTypeOrgId, accessToken);

    assertTrue(result.isEmpty());
  }

}
