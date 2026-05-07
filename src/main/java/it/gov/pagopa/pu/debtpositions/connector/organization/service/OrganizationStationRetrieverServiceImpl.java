package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import org.springframework.stereotype.Service;

import static it.gov.pagopa.pu.debtpositions.util.Constants.UNKNOWN_STATION_ID;

@Service
public class OrganizationStationRetrieverServiceImpl implements OrganizationStationRetrieverService {

  private final OrganizationService organizationService;

  public OrganizationStationRetrieverServiceImpl(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Override
  public OrganizationStationDTO getOrganizationStation(Long organizationId, String stationId, String accessToken) {
    if(UNKNOWN_STATION_ID.equalsIgnoreCase(stationId)) {
      OrganizationStationDTO organizationStationDTO = new OrganizationStationDTO();
      organizationStationDTO.setStationId(stationId);
      organizationStationDTO.setOrganizationId(organizationId);
      return organizationStationDTO;
    }
    return organizationService.getOrganizationStation(organizationId, stationId, accessToken)
      .orElseThrow(() -> new NotFoundException(
        ErrorCodeConstants.ERROR_CODE_ORGANIZATION_STATION_NOT_FOUND,
        "Unable to find organization station for organizationId %d and stationId %s".formatted(organizationId, stationId)
      ));
  }
}
