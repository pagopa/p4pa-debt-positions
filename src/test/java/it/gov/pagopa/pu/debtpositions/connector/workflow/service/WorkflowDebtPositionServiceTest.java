package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowDebtPositionApiClient;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.MassiveDebtPositionIbanUpdateRequestDTO;
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
class WorkflowDebtPositionServiceTest {

  @Mock
  private WorkflowDebtPositionApiClient workflowApiClientMock;

  private WorkflowDebtPositionService workflowService;

  @BeforeEach
  void init() {
    workflowService = new WorkflowDebtPositionServiceImpl(
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
  void whenSyncDebtPositionThenOk() {
    // Given
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    WfExecutionParameters wfExecutionParameters = new WfExecutionParameters();
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;
    String eventDescription = "EVENTDESCRIPTION";
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO("1", "runId");

    Mockito.when(workflowApiClientMock.syncDebtPosition(Mockito.same(debtPositionDTO), Mockito.same(wfExecutionParameters), Mockito.same(paymentEventType), Mockito.same(eventDescription), Mockito.same(accessToken)))
      .thenReturn(expectedResult);

    // When
    WorkflowCreatedDTO result = workflowService.syncDebtPosition(debtPositionDTO, wfExecutionParameters, paymentEventType, eventDescription, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void whenMassiveDpIbanUpdateThenOk() {
    String accessToken = "accessToken";
    Long orgId = 1L;
    MassiveDebtPositionIbanUpdateRequestDTO requestDTO = new MassiveDebtPositionIbanUpdateRequestDTO();
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();

    Mockito.when(workflowApiClientMock.massiveDpIbanUpdate(orgId, requestDTO, accessToken))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = workflowService.massiveDpIbanUpdate(orgId, requestDTO, accessToken);

    Assertions.assertSame(expectedResult, result);
  }
}
