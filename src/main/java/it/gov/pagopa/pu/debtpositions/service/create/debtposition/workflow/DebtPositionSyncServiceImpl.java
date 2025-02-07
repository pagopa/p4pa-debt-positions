package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.workflow.service.WorkflowService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class DebtPositionSyncServiceImpl implements DebtPositionSyncService {

  private final BrokerService brokerService;
  private final WorkflowService workflowService;

  private static final Set<DebtPositionOrigin> DEBT_POSITION_ORIGIN_TO_SYNC = Set.of(
    DebtPositionOrigin.ORDINARY,
    DebtPositionOrigin.ORDINARY_SIL,
    DebtPositionOrigin.SPONTANEOUS
  );

  public DebtPositionSyncServiceImpl(BrokerService brokerService, WorkflowService workflowService) {
    this.brokerService = brokerService;
    this.workflowService = workflowService;
  }

  @Override
  public WorkflowCreatedDTO invokeWorkFlow(DebtPositionDTO debtPositionDTO, String accessToken, Boolean pagopaPayment, Boolean massive) {
    if (Boolean.TRUE.equals(pagopaPayment) && DEBT_POSITION_ORIGIN_TO_SYNC.contains(debtPositionDTO.getDebtPositionOrigin())) {
      Optional<Broker> optBroker = brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);
      WorkflowCreatedDTO workflowCreatedDTO = null;

      if (optBroker.isPresent()) {
        Broker broker = optBroker.get();
        workflowCreatedDTO = switch (broker.getPagoPaInteractionModel()) {
          case SYNC ->
            workflowService.handleDpSync(debtPositionDTO, accessToken);
          case SYNC_ACA ->
            workflowService.alignDpSyncAca(debtPositionDTO, accessToken);
          case SYNC_GPDPRELOAD ->
            workflowService.alignDpSyncGpdPreload(debtPositionDTO, accessToken);
          case SYNC_ACA_GPDPRELOAD ->
            Boolean.TRUE.equals(massive) ? null : workflowService.alignDpSyncAcaGpdPreload(debtPositionDTO, accessToken);
          case ASYNC_GPD ->
            Boolean.TRUE.equals(massive) ? null : workflowService.alignDpGPD(debtPositionDTO, accessToken);
        };
      }

      return workflowCreatedDTO;
    }
    return null;
  }

}
