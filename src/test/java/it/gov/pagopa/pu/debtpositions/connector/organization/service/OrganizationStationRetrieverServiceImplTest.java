package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.Constants.UNKNOWN_STATION_ID;

@ExtendWith(MockitoExtension.class)
class OrganizationStationRetrieverServiceImplTest {

  private static final String ACCESS_TOKEN = "accessToken";

  @Mock
  private OrganizationService organizationServiceMock;
  @InjectMocks
  private OrganizationStationRetrieverServiceImpl organizationStationRetrieverService;

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationServiceMock
    );
  }

  @Test
  void givenNotExistingOrganizationStationWhenGetOrganizationStationThenEmpty(){
    // Given
    Long organizationId = 1L;
    String stationId = "STATION_ID";
    Mockito.when(organizationServiceMock.getOrganizationStation(organizationId, stationId, ACCESS_TOKEN))
      .thenReturn(Optional.empty());

    // When
    NotFoundException exception = Assertions.assertThrows(
      NotFoundException.class,
      () -> organizationStationRetrieverService.getOrganizationStation(organizationId, stationId, ACCESS_TOKEN)
    );

    // Then
    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_STATION_NOT_FOUND, exception.getCode());
    Assertions.assertEquals("Unable to find organization station for organizationId %d and stationId %s".formatted(organizationId, stationId), exception.getMessage());
    Mockito.verify(organizationServiceMock)
      .getOrganizationStation(organizationId, stationId, ACCESS_TOKEN);
  }

  @Test
  void givenExistentOrganizationStationWhenGetOrganizationStationThenOk(){
    // Given
    Long organizationId = 1L;
    String stationId = "STATION_ID";
    Optional<OrganizationStationDTO> expectedResult = Optional.of(new OrganizationStationDTO());
    Mockito.when(organizationServiceMock.getOrganizationStation(organizationId, stationId, ACCESS_TOKEN))
      .thenReturn(expectedResult);

    // When
    OrganizationStationDTO result = organizationStationRetrieverService.getOrganizationStation(organizationId, stationId, ACCESS_TOKEN);

    // Then
    Assertions.assertEquals(expectedResult.get(), result);
    Mockito.verify(organizationServiceMock)
      .getOrganizationStation(organizationId, stationId, ACCESS_TOKEN);
  }

  @Test
  void givenUnknownStationIdWhenGetOrganizationStationThenReturnFakeOrganizationStationWithUnknownStationId(){
    // Given
    Long organizationId = 1L;
    OrganizationStationDTO expectedOrganizationStationDTO = new OrganizationStationDTO();
    expectedOrganizationStationDTO.setOrganizationId(organizationId);
    expectedOrganizationStationDTO.setStationId(UNKNOWN_STATION_ID);

    // When
    OrganizationStationDTO result = organizationStationRetrieverService.getOrganizationStation(organizationId, UNKNOWN_STATION_ID, ACCESS_TOKEN);

    // Then
    Assertions.assertEquals(expectedOrganizationStationDTO, result);
    Mockito.verify(organizationServiceMock, Mockito.times(0))
      .getOrganizationStation(organizationId, UNKNOWN_STATION_ID, ACCESS_TOKEN);
  }

}
