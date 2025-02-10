package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Broker;

import java.util.Optional;

public interface BrokerService {

  Optional<Broker> getBrokerByOrganizationId(Long organizationId, String accessToken);
}
