package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.workflow.DebtPositionRequestMapper;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DebtPositionSyncServiceImpl implements DebtPositionSyncService {

  private final BrokerService brokerService;
  private final WorkflowService workflowService;
  private final DebtPositionRequestMapper debtPositionRequestMapper;

  public DebtPositionSyncServiceImpl(BrokerService brokerService, WorkflowService workflowService, DebtPositionRequestMapper debtPositionRequestMapper) {
    this.brokerService = brokerService;
    this.workflowService = workflowService;
    this.debtPositionRequestMapper = debtPositionRequestMapper;
  }

  @Override
  public WorkflowCreatedDTO invokeWorkFlow(DebtPositionDTO debtPositionDTO, String accessToken) {
    Optional<Broker> optBroker = brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);
    DebtPositionRequestDTO debtPositionRequest = debtPositionRequestMapper.map(debtPositionDTO);
    WorkflowCreatedDTO workflowCreatedDTO = null;
    if (optBroker.isPresent()) {
      Broker broker = optBroker.get();
      workflowCreatedDTO = switch (broker.getPagoPaInteractionModel()) {
        case SYNC ->
          workflowService.handleDpSync(debtPositionRequest, accessToken);
        case SYNC_ACA ->
          workflowService.alignDpSyncAca(debtPositionRequest, accessToken);
        case SYNC_GPDPRELOAD ->
          workflowService.alignDpSyncGpdPreload(debtPositionRequest, accessToken);
        case SYNC_ACA_GPDPRELOAD ->
          workflowService.alignDpSyncAcaGpdPreload(debtPositionRequest, accessToken);
        case ASYNC_GPD ->
          workflowService.alignDpGPD(debtPositionRequest, accessToken);
        case null -> null;
      };
    }
    return workflowCreatedDTO;
  }
}
