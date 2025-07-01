package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.TaxonomySearchClient;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomyEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TaxonomyServiceTest {

  @Mock
  private TaxonomySearchClient taxonomySearchClient;

  private TaxonomyService taxonomyService;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    taxonomyService = new TaxonomyServiceImpl(taxonomySearchClient);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      taxonomySearchClient
    );
  }

  @Test
  void givenOneTaxonomyWhenFindTaxonomiesThenSuccess() {
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(taxonomySearchClient.findTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 10, null, accessToken))
      .thenReturn(pagedModelTaxonomy);

    // When
    PagedModelTaxonomy result = taxonomyService.getTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, 0, 10, null, accessToken);

    // Then
    assertEquals(result, pagedModelTaxonomy);
  }
}
