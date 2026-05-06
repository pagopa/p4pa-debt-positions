package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;

import java.util.Optional;

public interface OrganizationService {

  Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode, String accessToken);

  Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken);

  Optional<Organization> getOrganizationById(Long id, String accessToken);

  Optional<OrganizationStationDTO> getOrganizationStation(Long organizationId, String stationId, String accessToken);

}
