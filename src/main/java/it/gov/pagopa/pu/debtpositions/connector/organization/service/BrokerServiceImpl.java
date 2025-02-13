package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerEntityClient;
import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrokerServiceImpl implements BrokerService {

  private final BrokerSearchClient brokerSearchClient;
  private final BrokerEntityClient brokerEntityClient;

  public BrokerServiceImpl(BrokerSearchClient brokerSearchClient, BrokerEntityClient brokerEntityClient) {
    this.brokerSearchClient = brokerSearchClient;
    this.brokerEntityClient = brokerEntityClient;
  }

  @Override
  public Optional<Broker> getBrokerByOrganizationId(Long organizationId, String accessToken) {
    return Optional.ofNullable(
      brokerSearchClient.findByOrganizationId(organizationId, accessToken)
    );
  }

  @Override
  public Broker findById(Long brokerId, String accessToken) {
    return brokerEntityClient.findById(brokerId, accessToken);
  }
}
