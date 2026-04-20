package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import org.springframework.stereotype.Service;

@Service
public class BrokerClient {

  private final OrganizationApisHolder organizationApisHolder;

  public BrokerClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Broker findById(Long brokerId, String accessToken) {
    return organizationApisHolder.getBrokerEntityControllerApi(accessToken)
      .crudGetBroker(String.valueOf(brokerId));
  }

  public BrokerConfiguration getBrokerConfigurationById(Long brokerId, String accessToken) {
    return organizationApisHolder.getBrokerConfigurationEntityControllerApi(accessToken)
      .crudGetBrokerconfiguration(brokerId.toString());
  }

}
