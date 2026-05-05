package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStation;

import java.util.Optional;

public interface OrganizationService {

  Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode, String accessToken);

  Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken);

  Optional<Organization> getOrganizationById(Long id, String accessToken);

  void verifyStationId(Long organizationId, String stationId, String accessToken);

  OrganizationStation getDefaultOrganizationStation(Long organizationId, String accessToken);

}
