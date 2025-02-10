package it.gov.pagopa.pu.debtpositions.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Broker;

public interface BrokerService {

  Broker findById(Long brokerId, String accessToken);
}
