package it.gov.pagopa.pu.debtpositions.connector.classification.client;

import it.gov.pagopa.pu.classification.controller.generated.AssessmentsRegistrySearchControllerApi;
import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;
import it.gov.pagopa.pu.debtpositions.connector.classification.config.ClassificationApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class AssessmentsRegistryClientTest {
  @Mock
  private ClassificationApisHolder classificationApisHolder;
  @Mock
  private AssessmentsRegistrySearchControllerApi assessmentsRegistrySearchControllerApiMock;

  private AssessmentsRegistryClient assessmentsRegistryClient;

  @BeforeEach
  void setUp() {
    assessmentsRegistryClient = new AssessmentsRegistryClient(classificationApisHolder);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      classificationApisHolder
    );
  }

  @Test
  void givenValidFiltersWhenFindAssessmentsRegistriesThenReturnPagedModel() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    Long organizationId = 1L;
    Set<String> debtPositionTypeOrgCodes = Set.of("debtPositionTypeCode");
    String assessmentCode = "ASS001";
    String operatingYear = "2025";
    AssessmentsRegistryStatus status = AssessmentsRegistryStatus.ACTIVE;

    PagedModelAssessmentsRegistry mockedResponse = new PagedModelAssessmentsRegistry();

    Mockito.when(classificationApisHolder.getAssessmentsRegistrySearchControllerApi(accessToken))
      .thenReturn(assessmentsRegistrySearchControllerApiMock);
    Mockito.when(assessmentsRegistrySearchControllerApiMock.crudAssessmentsRegistriesFindAssessmentsRegistriesByFilters(
      organizationId,
      debtPositionTypeOrgCodes,
      null,
      null,
      null,
      null,
      assessmentCode,
      null,
      operatingYear,
      status,
      0,
      1,
      null
    )).thenReturn(mockedResponse);

    // When
    PagedModelAssessmentsRegistry result = assessmentsRegistryClient.findAssessmentsRegistriesByFilters(
      organizationId,
      debtPositionTypeOrgCodes,
      null,
      null,
      null,
      null,
      assessmentCode,
      null,
      operatingYear,
      status,
      0,
      1,
      null,
      accessToken
    );

    // Then
    assertSame(mockedResponse, result);
  }
}
