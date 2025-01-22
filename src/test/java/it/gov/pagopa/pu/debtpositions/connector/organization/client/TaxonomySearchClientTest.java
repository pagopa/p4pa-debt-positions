package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.client.generated.TaxonomySearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaxonomySearchClientTest {
    @Mock
    private OrganizationApisHolder organizationApisHolder;
    @Mock
    private TaxonomySearchControllerApi taxonomySearchControllerApi;

    private TaxonomySearchClient taxonomySearchClient;

    @BeforeEach
    void setUp() {
        taxonomySearchClient = new TaxonomySearchClient(organizationApisHolder);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
          organizationApisHolder
        );
    }

    @Test
    void whenFindByTaxonomyCodeThenInvokeWithAccessToken(){
        // Given
        String accessToken = "ACCESSTOKEN";
        String taxonomyCode = "TAXONOMYCODE";
        Taxonomy expectedResult = new Taxonomy();

        Mockito.when(organizationApisHolder.getTaxonomyCodeDtoSearchControllerApi(accessToken))
                .thenReturn(taxonomySearchControllerApi);
        Mockito.when(taxonomySearchControllerApi.crudTaxonomiesFindByTaxonomyCode(taxonomyCode))
                .thenReturn(expectedResult);

        // When
        Taxonomy result = taxonomySearchClient.findByTaxonomyCode(taxonomyCode, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
