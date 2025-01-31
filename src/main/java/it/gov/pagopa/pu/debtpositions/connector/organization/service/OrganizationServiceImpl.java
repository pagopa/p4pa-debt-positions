package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationSearchClient organizationSearchClient;

  public OrganizationServiceImpl(OrganizationSearchClient organizationSearchClient) {
    this.organizationSearchClient = organizationSearchClient;
  }

  @Override
  public Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByOrgFiscalCode(orgFiscalCode, accessToken)
    );
  }

  @Override
  public Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByIpaCode(ipaCode, accessToken)
    );
  }

  @Override
  public Optional<Organization> getOrganizationById(Long orgId, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByOrganizationId(orgId, accessToken)
    );
  }
}
