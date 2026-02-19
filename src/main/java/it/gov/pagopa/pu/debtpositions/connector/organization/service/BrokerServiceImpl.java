package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerClient;
import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.broker)
public class BrokerServiceImpl implements BrokerService {

  private final BrokerSearchClient brokerSearchClient;
  private final BrokerClient brokerClient;

  public BrokerServiceImpl(BrokerSearchClient brokerSearchClient, BrokerClient brokerClient) {
    this.brokerSearchClient = brokerSearchClient;
    this.brokerClient = brokerClient;
  }

  @Override
  public Optional<Broker> getBrokerByBrokeredOrganizationId(Long organizationId, String accessToken) {
    return Optional.ofNullable(
      brokerSearchClient.findByBrokeredOrganizationId(organizationId, accessToken)
    );
  }

  @Override
  @Cacheable(key = "'brokerId-' + #brokerId", unless = "#result == null")
  public Broker findById(Long brokerId, String accessToken) {
    return brokerClient.findById(brokerId, accessToken);
  }
}
