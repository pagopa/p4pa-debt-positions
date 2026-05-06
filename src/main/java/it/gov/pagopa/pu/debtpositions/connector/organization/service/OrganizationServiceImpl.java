package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.organization)
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationSearchClient organizationSearchClient;

  public OrganizationServiceImpl(OrganizationSearchClient organizationSearchClient) {
    this.organizationSearchClient = organizationSearchClient;
  }

  @Override
  @Cacheable(key = "'fiscalCode-' + #orgFiscalCode", unless = "#result == null")
  public Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByOrgFiscalCode(orgFiscalCode, accessToken)
    );
  }

  @Override
  @Cacheable(key = "'ipaCode-' + #ipaCode", unless = "#result == null")
  public Optional<Organization> getOrganizationByIpaCode(String ipaCode, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByIpaCode(ipaCode, accessToken)
    );
  }

  @Override
  @Cacheable(key = "'orgId-' + #orgId", unless = "#result == null")
  public Optional<Organization> getOrganizationById(Long orgId, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findByOrganizationId(orgId, accessToken)
    );
  }

  @Override
  @Cacheable(key = "'organizationId-' + #organizationId + '_stationId-' + #stationId", unless = "#result == null")
  public Optional<OrganizationStationDTO> getOrganizationStation(Long organizationId, String stationId, String accessToken) {
    return Optional.ofNullable(
      organizationSearchClient.findOrganizationStationByOrganizationIdAndStationId(organizationId, stationId, accessToken)
    );
  }
}
