package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionSyncServiceImplTest {
  @Mock
  private BrokerService brokerService;
  @Mock
  private WorkflowService workflowService;

  private DebtPositionSyncServiceImpl debtPositionSyncService;

  @BeforeEach
  void setUp() {
    debtPositionSyncService = new DebtPositionSyncServiceImpl(brokerService,
      workflowService);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNCThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(workflowService.handleDpSync(debtPositionDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null,false);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNC_ACAThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_ACA);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(workflowService.alignDpSyncAca(debtPositionDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNC_GPDPRELOADThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_GPDPRELOAD);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(workflowService.alignDpSyncGpdPreload(debtPositionDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNC_ACA_GPDPRELOADThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_ACA_GPDPRELOAD);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(workflowService.alignDpSyncAcaGpdPreload(debtPositionDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenMassiveTrueWhenInvokeWorkflowSYNC_ACA_GPDPRELOADThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_ACA_GPDPRELOAD);

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, true);

    assertNull(result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowASYNC_GPDThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.ASYNC_GPD);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(workflowService.alignDpGPD(debtPositionDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenMassiveTrueWhenInvokeWorkflowASYNC_GPDThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.ASYNC_GPD);

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, true);

    assertNull(result);
  }

  @Test
  void givenInvalidbrokerWhenInvokeWorkflowASYNC_GPDThenNull() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPagoPaPayment(true);

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.empty());

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertNull(result);
  }

  @Test
  void givenOriginDifferentWhenInvokeWorkflowASYNC_GPDThenNull() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_FILE);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertNull(result);
  }

  @Test
  void givenPagoPaPaymentFalseWhenInvokeWorkflowASYNC_GPDThenNull() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null, false);

    assertNull(result);
  }
}
