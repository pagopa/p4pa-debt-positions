package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;
import it.gov.pagopa.pu.debtpositions.connector.classification.client.AssessmentsRegistryClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AssessmentsRegistryServiceTest {

  @Mock
  private AssessmentsRegistryClient assessmentsRegistryClientMock;

  private AssessmentsRegistryService assessmentsRegistryService;

  @BeforeEach
  void init() {
    assessmentsRegistryService = new AssessmentsRegistryServiceImpl(assessmentsRegistryClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(assessmentsRegistryClientMock);
  }

  @Test
  void givenAssessmentsRegistryValidWhenValidateThenTrue() {
    // Given
    PagedModelAssessmentsRegistry mockedResponse = new PagedModelAssessmentsRegistry();
    String accessToken = "ACCESSTOKEN";

    Mockito.when(assessmentsRegistryClientMock.findAssessmentsRegistriesByFilters(
        1L,
        Set.of("200L"),
        null,
        null,
        null,
        null,
        "assessmentCode",
        null,
        "operatingYear",
        AssessmentsRegistryStatus.ACTIVE,
        0,
        1,
        null,
        accessToken
      ))
      .thenReturn(mockedResponse);

    // When
    PagedModelAssessmentsRegistry result = assessmentsRegistryService.findAssessmentsRegistriesByFilters(
      1L,
      Set.of("200L"),
      null,
      null,
      null,
      null,
      "assessmentCode",
      null,
      "operatingYear",
      AssessmentsRegistryStatus.ACTIVE,
      0,
      1,
      null,
      accessToken);

    // Then
    Assertions.assertEquals(mockedResponse, result);
  }
}
