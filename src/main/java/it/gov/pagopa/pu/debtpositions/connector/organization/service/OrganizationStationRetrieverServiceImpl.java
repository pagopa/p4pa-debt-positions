package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import org.springframework.stereotype.Service;

@Service
public class OrganizationStationRetrieverServiceImpl implements OrganizationStationRetrieverService {

  private final OrganizationService organizationService;

  public OrganizationStationRetrieverServiceImpl(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Override
  public OrganizationStationDTO getOrganizationStation(Long organizationId, String stationId, String accessToken) {
    return organizationService.getOrganizationStation(organizationId, stationId, accessToken)
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_ORGANIZATION_STATION_NOT_FOUND,
        "Unable to find organization station for organizationId %d and stationId %s".formatted(organizationId, stationId)
      ));
  }
}
