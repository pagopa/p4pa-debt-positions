package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;

public interface OrganizationStationRetrieverService {
  OrganizationStationDTO getOrganizationStation(Long organizationId, String stationId, String accessToken);
}
