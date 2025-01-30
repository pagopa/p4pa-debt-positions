package it.gov.pagopa.pu.debtpositions.service.create.debtposition.workflow;

import it.gov.pagopa.pu.debtpositions.connector.organization.BrokerService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionSyncServiceImpl implements DebtPositionSyncService {

  private final BrokerService brokerService;

  public DebtPositionSyncServiceImpl(BrokerService brokerService) {
    this.brokerService = brokerService;
  }

  @Override
  public void invokeWorkFlow(DebtPositionDTO debtPositionDTO, String accessToken) {
    brokerService.getBrokerByOrganizationId(debtPositionDTO.getOrganizationId(), accessToken);
  }
}
