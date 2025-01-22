package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationSearchClientTest {
    @Mock
    private OrganizationApisHolder organizationApisHolderMock;
    @Mock
    private OrganizationSearchControllerApi organizationSearchControllerApiMock;

    private OrganizationSearchClient organizationSearchClient;

    @BeforeEach
    void setUp() {
        organizationSearchClient = new OrganizationSearchClient(organizationApisHolderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                organizationApisHolderMock
        );
    }

    @Test
    void whenFindByIpaCodeThenInvokeWithAccessToken(){
        // Given
        String accessToken = "ACCESSTOKEN";
        String orgIpaCode = "ORGIPACODE";
        Organization expectedResult = new Organization();

        Mockito.when(organizationApisHolderMock.getOrganizationSearchControllerApi(accessToken))
                .thenReturn(organizationSearchControllerApiMock);
        Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByIpaCode(orgIpaCode))
                .thenReturn(expectedResult);

        // When
        Organization result = organizationSearchClient.findByIpaCode(orgIpaCode, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
    }

    @Test
    void whenGetOrgFiscalCodeThenInvokeWithAccessToken(){
        // Given
        String accessToken = "ACCESSTOKEN";
        String orgFiscalCode = "ORGFISCALCODE";
        Organization expectedResult = new Organization();

        Mockito.when(organizationApisHolderMock.getOrganizationSearchControllerApi(accessToken))
                .thenReturn(organizationSearchControllerApiMock);
        Mockito.when(organizationSearchControllerApiMock.crudOrganizationsFindByOrgFiscalCode(orgFiscalCode))
                .thenReturn(expectedResult);

        // When
        Organization result = organizationSearchClient.findByOrgFiscalCode(orgFiscalCode, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
