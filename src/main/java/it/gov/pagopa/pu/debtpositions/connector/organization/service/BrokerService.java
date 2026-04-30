package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;

import java.util.Optional;

public interface BrokerService {

  Optional<Broker> getBrokerByBrokeredOrganizationId(Long organizationId, String accessToken);

  Broker findById(Long brokerId, String accessToken);

  BrokerConfiguration getBrokerConfigurationsById(Long brokerId, String accessToken);
}
