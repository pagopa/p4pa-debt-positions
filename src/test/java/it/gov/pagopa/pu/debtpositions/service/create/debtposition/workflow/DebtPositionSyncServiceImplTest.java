package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.workflow.DebtPositionRequestMapper;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionRequestDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionSyncServiceImplTest {
  @Mock
  private BrokerService brokerService;
  @Mock
  private WorkflowService workflowService;
  @Mock
  private DebtPositionRequestMapper debtPositionRequestMapper;

  private DebtPositionSyncServiceImpl debtPositionSyncService;

  @BeforeEach
  void setUp() {
    debtPositionSyncService = new DebtPositionSyncServiceImpl(brokerService,
      workflowService, debtPositionRequestMapper);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNCThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(debtPositionRequestMapper.map(debtPositionDTO)).thenReturn(debtPositionRequestDTO);
    Mockito.when(workflowService.handleDpSync(debtPositionRequestDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNC_ACAThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_ACA);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(debtPositionRequestMapper.map(debtPositionDTO)).thenReturn(debtPositionRequestDTO);
    Mockito.when(workflowService.alignDpSyncAca(debtPositionRequestDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNC_GPDPRELOADThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_GPDPRELOAD);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(debtPositionRequestMapper.map(debtPositionDTO)).thenReturn(debtPositionRequestDTO);
    Mockito.when(workflowService.alignDpSyncGpdPreload(debtPositionRequestDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowSYNC_ACA_GPDPRELOADThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.SYNC_ACA_GPDPRELOAD);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(debtPositionRequestMapper.map(debtPositionDTO)).thenReturn(debtPositionRequestDTO);
    Mockito.when(workflowService.alignDpSyncAcaGpdPreload(debtPositionRequestDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenDebtPositionWhenInvokeWorkflowASYNC_GPDThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();
    Broker broker = new Broker();
    broker.setPagoPaInteractionModel(Broker.PagoPaInteractionModelEnum.ASYNC_GPD);
    WorkflowCreatedDTO expectedResult = new WorkflowCreatedDTO();
    expectedResult.setWorkflowId("1000");

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.of(broker));
    Mockito.when(debtPositionRequestMapper.map(debtPositionDTO)).thenReturn(debtPositionRequestDTO);
    Mockito.when(workflowService.alignDpGPD(debtPositionRequestDTO, null)).thenReturn(expectedResult);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null);

    assertEquals(expectedResult, result);
  }

  @Test
  void givenInvalidbrokerWhenInvokeWorkflowASYNC_GPDThenNull() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionRequestDTO debtPositionRequestDTO = buildDebtPositionRequestDTO();

    Mockito.when(brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), null)).thenReturn(Optional.empty());
    Mockito.when(debtPositionRequestMapper.map(debtPositionDTO)).thenReturn(debtPositionRequestDTO);

    WorkflowCreatedDTO result = debtPositionSyncService.invokeWorkFlow(debtPositionDTO, null);

    assertNull(result);
  }
}
