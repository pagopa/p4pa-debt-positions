package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidParamException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStation;
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
  public void verifyStationId(Long organizationId, String stationId, String accessToken) {
    Optional.ofNullable(organizationSearchClient.findOrganizationStationByOrganizationIdAndStationId(organizationId, stationId, accessToken))
      .orElseThrow(() -> new InvalidParamException(
        ErrorCodeConstants.ERROR_CODE_INVALID_STATION_ID,
        "Invalid station id %s for Organization %d".formatted(stationId, organizationId)
      ));
  }

  @Override
  public OrganizationStation getDefaultOrganizationStation(Long organizationId, String accessToken) {
    return Optional.ofNullable(organizationSearchClient.findOrganizationStationByOrganizationIdAndStationId(organizationId, null, accessToken))
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_DEFAULT_ORGANIZATION_STATION_NOT_FOUND,
        "Unable to find a default organization station for Organization %d".formatted(organizationId)
      ));
  }
}
