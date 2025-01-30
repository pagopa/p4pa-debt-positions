package it.gov.pagopa.pu.debtpositions.connector.organization;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrokerServiceImpl implements BrokerService {

  private final BrokerSearchClient brokerSearchClient;

  public BrokerServiceImpl(BrokerSearchClient brokerSearchClient) {
    this.brokerSearchClient = brokerSearchClient;
  }

  @Override
  public Optional<Broker> getBrokerByOrganizationId(Long organizationId, String accessToken) {
    return Optional.ofNullable(
      brokerSearchClient.findByOrganizationId(organizationId, accessToken)
    );
  }
}
