package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.springframework.stereotype.Service;

@Service
public class BrokerEntityClient {

  private final OrganizationApisHolder organizationApisHolder;

  public BrokerEntityClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Broker findById(Long brokerId, String accessToken) {
    return organizationApisHolder.getBrokerEntityControllerApi(accessToken)
      .crudGetBroker(String.valueOf(brokerId));
  }

}
