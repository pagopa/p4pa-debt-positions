package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidParamException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationSearchClient organizationSearchClientMock;

    private OrganizationService organizationService;

    private final String accessToken = "ACCESSTOKEN";

    @BeforeEach
    void init(){
        organizationService = new OrganizationServiceImpl(
                organizationSearchClientMock
        );
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                organizationSearchClientMock
        );
    }

//region getOrganizationByFiscalCode tests
    @Test
    void givenNotExistentFiscalCodeWhenGetOrganizationByFiscalCodeThenEmpty(){
        // Given
        String orgFiscalCode = "ORGFISCALCODE";
        Mockito.when(organizationSearchClientMock.findByOrgFiscalCode(orgFiscalCode, accessToken))
                .thenReturn(null);

        // When
        Optional<Organization> result = organizationService.getOrganizationByFiscalCode(orgFiscalCode, accessToken);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void givenExistentFiscalCodeWhenGetOrganizationByFiscalCodeThenEmpty(){
        // Given
        String orgFiscalCode = "ORGFISCALCODE";
        Organization expectedResult = new Organization();
        Mockito.when(organizationSearchClientMock.findByOrgFiscalCode(orgFiscalCode, accessToken))
                .thenReturn(expectedResult);

        // When
        Optional<Organization> result = organizationService.getOrganizationByFiscalCode(orgFiscalCode, accessToken);

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertSame(expectedResult, result.get());
    }
//endregion

//region getOrganizationByIpaCode tests
    @Test
    void givenNotExistentFiscalCodeWhenGetOrganizationByIpaCodeThenEmpty(){
        // Given
        String orgIpaCode = "ORGIPACODE";
        Mockito.when(organizationSearchClientMock.findByIpaCode(orgIpaCode, accessToken))
                .thenReturn(null);

        // When
        Optional<Organization> result = organizationService.getOrganizationByIpaCode(orgIpaCode, accessToken);

        // Then
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void givenExistentFiscalCodeWhenGetOrganizationByIpaCodeThenEmpty(){
        // Given
        String orgIpaCode = "ORGIPACODE";
        Organization expectedResult = new Organization();
        Mockito.when(organizationSearchClientMock.findByIpaCode(orgIpaCode, accessToken))
                .thenReturn(expectedResult);

        // When
        Optional<Organization> result = organizationService.getOrganizationByIpaCode(orgIpaCode, accessToken);

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertSame(expectedResult, result.get());
    }
//endregion

//region getOrganizationById tests
  @Test
  void givenNotExistentOrgIdWhenGetOrganizationByIdThenEmpty(){
    // Given
    Long orgId = 1L;
    Mockito.when(organizationSearchClientMock.findByOrganizationId(orgId, accessToken))
      .thenReturn(null);

    // When
    Optional<Organization> result = organizationService.getOrganizationById(orgId, accessToken);

    // Then
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void givenExistentOrgIdWhenGetOrganizationByIdThenEmpty(){
    // Given
    Long orgId = 1L;
    Organization expectedResult = new Organization();
    Mockito.when(organizationSearchClientMock.findByOrganizationId(orgId, accessToken))
      .thenReturn(expectedResult);

    // When
    Optional<Organization> result = organizationService.getOrganizationById(orgId, accessToken);

    // Then
    Assertions.assertTrue(result.isPresent());
    Assertions.assertSame(expectedResult, result.get());
  }
//endregion

//region verifyOrganizationStation tests
  @Test
  void givenNotExistentOrganizationStationWhenVerifyStationIdThenThrowInvalidParamException(){
    // Given
    Long organizationId = 1L;
    String stationId = "STATION_ID";
    Mockito.when(organizationSearchClientMock.findOrganizationStationByOrganizationIdAndStationId(organizationId, stationId, accessToken))
      .thenReturn(null);

    // When
    InvalidParamException exception = Assertions.assertThrows(
      InvalidParamException.class,
      () -> organizationService.verifyStationId(organizationId, stationId, accessToken)
    );

    // Then
    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_INVALID_STATION_ID, exception.getCode());
    Assertions.assertEquals("Invalid station id %s for Organization %d".formatted(stationId, organizationId), exception.getMessage());
    Mockito.verify(organizationSearchClientMock)
      .findOrganizationStationByOrganizationIdAndStationId(organizationId, stationId, accessToken);
  }

  @Test
  void givenExistentOrganizationStationWhenVerifyStationIdIdThenOk(){
    // Given
    Long organizationId = 1L;
    String stationId = "STATION_ID";
    OrganizationStation expectedResult = new OrganizationStation();
    Mockito.when(organizationSearchClientMock.findOrganizationStationByOrganizationIdAndStationId(organizationId, stationId, accessToken))
      .thenReturn(expectedResult);

    // When
    organizationService.verifyStationId(organizationId, stationId, accessToken);

    // Then
    Mockito.verify(organizationSearchClientMock)
      .findOrganizationStationByOrganizationIdAndStationId(organizationId, stationId, accessToken);
  }
//endregion

  //region verifyOrganizationStation tests
  @Test
  void givenNotExistentOrganizationStationWhenGetDefaultOrganizationStationThenThrowNotFoundException(){
    // Given
    Long organizationId = 1L;
    Mockito.when(
      organizationSearchClientMock.findOrganizationStationByOrganizationIdAndStationId(
        Mockito.eq(organizationId),
        Mockito.isNull(),
        Mockito.eq(accessToken)
      )
    ).thenReturn(null);

    // When
    NotFoundException exception = Assertions.assertThrows(
      NotFoundException.class,
      () -> organizationService.getDefaultOrganizationStation(organizationId, accessToken)
    );

    // Then
    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_DEFAULT_ORGANIZATION_STATION_NOT_FOUND, exception.getCode());
    Assertions.assertEquals("Unable to find a default organization station for Organization %d".formatted(organizationId), exception.getMessage());
    Mockito.verify(organizationSearchClientMock)
      .findOrganizationStationByOrganizationIdAndStationId(
        Mockito.eq(organizationId),
        Mockito.isNull(),
        Mockito.eq(accessToken)
      );
  }

  @Test
  void givenExistentOrganizationStationWhenGetDefaultOrganizationStationThenOk(){
    // Given
    Long organizationId = 1L;
    OrganizationStation expectedResult = new OrganizationStation();
    Mockito.when(
      organizationSearchClientMock.findOrganizationStationByOrganizationIdAndStationId(
        Mockito.eq(organizationId),
        Mockito.isNull(),
        Mockito.eq(accessToken)
      )
    ).thenReturn(expectedResult);

    // When
    OrganizationStation defaultOrganizationStation = organizationService.getDefaultOrganizationStation(organizationId, accessToken);

    // Then
    Assertions.assertEquals(expectedResult, defaultOrganizationStation);
    Mockito.verify(organizationSearchClientMock)
      .findOrganizationStationByOrganizationIdAndStationId(
        Mockito.eq(organizationId),
        Mockito.isNull(),
        Mockito.eq(accessToken)
      );
  }
//endregion
}
