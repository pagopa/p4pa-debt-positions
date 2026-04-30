package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.client.generated.TaxonomySearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomyEmbedded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;

@ExtendWith(MockitoExtension.class)
class TaxonomySearchClientTest {

  @Mock
  private OrganizationApisHolder organizationApisHolderMock;
  @Mock
  private TaxonomySearchControllerApi taxonomySearchControllerApiMock;

  private TaxonomySearchClient taxonomySearchClient;

  @BeforeEach
  void setUp() {
    taxonomySearchClient = new TaxonomySearchClient(organizationApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolderMock,
      taxonomySearchControllerApiMock
    );
  }

  @Test
  void whenFindTaxonomiesThenInvokeWithAccessToken() {
    // Given
    String organizationType = "00";
    String macroAreaCode = "11";
    String serviceTypeCode = "222";
    String collectionReason = "33";
    String accessToken = "ACCESSTOKEN";

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(organizationApisHolderMock.getTaxonomySearchControllerApi(accessToken))
      .thenReturn(taxonomySearchControllerApiMock);
    Mockito.when(taxonomySearchControllerApiMock.crudTaxonomiesFindTaxonomies(organizationType, macroAreaCode,
        serviceTypeCode, collectionReason, 0, 1, null))
      .thenReturn(pagedModelTaxonomy);

    // When
    PagedModelTaxonomy result = taxonomySearchClient.findTaxonomies(organizationType, macroAreaCode,
      serviceTypeCode, collectionReason, 0, 1, null, accessToken);

    // Then
    Assertions.assertSame(pagedModelTaxonomy, result);
  }
}
