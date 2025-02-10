package it.gov.pagopa.pu.debtpositions.connector.organization;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerEntityClient;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BrokerServiceImpl implements BrokerService {

  private final BrokerEntityClient brokerEntityClient;

  public BrokerServiceImpl(BrokerEntityClient brokerEntityClient) {
    this.brokerEntityClient = brokerEntityClient;
  }

  @Override
  @Cacheable
  public Broker findById(Long brokerId, String accessToken) {
  return brokerEntityClient.findById(brokerId, accessToken);
  }

}
