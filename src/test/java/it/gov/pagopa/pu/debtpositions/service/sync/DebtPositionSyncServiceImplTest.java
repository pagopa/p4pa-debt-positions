package it.gov.pagopa.pu.debtpositions.service.sync;

import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionSyncServiceImplTest {

  @Mock
  private WorkflowService workflowService;

  private DebtPositionSyncServiceImpl debtPositionSyncService;

  @BeforeEach
  void setUp() {
    debtPositionSyncService = new DebtPositionSyncServiceImpl(workflowService);
  }

  @Test
  void givenAllowedOriginWhenSyncDebtPositionThenInvokeWF() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    Boolean massive = true;
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(workflowService.syncDebtPosition(debtPositionDTO, massive, paymentEventType, accessToken))
      .thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.syncDebtPosition(debtPositionDTO, massive, paymentEventType, accessToken);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenNotAllowedOriginWhenSyncDebtPositionThenInvokeWF() {
    String accessToken = "ACCESSTOKEN";
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_PAGOPA);
    Boolean massive = true;
    PaymentEventType paymentEventType = PaymentEventType.DP_CREATED;

    WorkflowCreatedDTO result = debtPositionSyncService.syncDebtPosition(debtPositionDTO, massive, paymentEventType, accessToken);

    assertNull(result);
  }

}
