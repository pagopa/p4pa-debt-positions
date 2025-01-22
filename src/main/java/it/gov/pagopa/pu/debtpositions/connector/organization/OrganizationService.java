package it.gov.pagopa.pu.debtpositions.connector.organization;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

import java.util.Optional;

public interface OrganizationService {

  Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode, String accessToken);

  Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken);

}
