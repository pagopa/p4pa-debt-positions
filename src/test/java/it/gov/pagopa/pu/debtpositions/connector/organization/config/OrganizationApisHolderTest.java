package it.gov.pagopa.pu.debtpositions.connector.organization.config;

import it.gov.pagopa.pu.debtpositions.connector.BaseApiHolderTest;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import it.gov.pagopa.pu.organization.generated.ApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class OrganizationApisHolderTest extends BaseApiHolderTest {
    @Mock
    private RestTemplateBuilder restTemplateBuilderMock;

    private OrganizationApisHolder organizationApisHolder;

    @BeforeEach
    void setUp() {
        Mockito.when(restTemplateBuilderMock.build()).thenReturn(restTemplateMock);
        Mockito.when(restTemplateMock.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
        ApiClient apiClient = new ApiClient(restTemplateMock);
        String baseUrl = "http://example.com";
        apiClient.setBasePath(baseUrl);
        organizationApisHolder = new OrganizationApisHolder(baseUrl, restTemplateBuilderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(
                restTemplateBuilderMock,
                restTemplateMock
        );
    }

    @Test
    void whenGetOrganizationSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
        assertAuthenticationShouldBeSetInThreadSafeMode(
                accessToken -> organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
                        .crudOrganizationsFindByIpaCode("ORGIPACODE"),
                Organization.class,
                organizationApisHolder::unload);
    }

  @Test
  void whenGetTaxonomyCodeDtoSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getTaxonomyCodeDtoSearchControllerApi(accessToken)
        .crudTaxonomiesFindByTaxonomyCode("TAXONOMYCODE"),
      Taxonomy.class,
      organizationApisHolder::unload);
  }

  @Test
  void whenGetOrganizationEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getOrganizationEntityControllerApi(accessToken)
        .crudGetOrganization("ORGID"),
      Organization.class,
      organizationApisHolder::unload);
  }

  @Test
  void whenGetBrokerEntityControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getBrokerEntityControllerApi(accessToken)
        .crudGetBroker("BROKER_ID"),
      Broker.class,
      organizationApisHolder::unload);
  }

  @Test
  void whenGetBrokerSearchControllerApiThenAuthenticationShouldBeSetInThreadSafeMode() throws InterruptedException {
    assertAuthenticationShouldBeSetInThreadSafeMode(
      accessToken -> organizationApisHolder.getBrokerSearchControllerApi(accessToken)
        .crudBrokersFindByBrokeredOrganizationId("ORGID"),
      Broker.class,
      organizationApisHolder::unload);
  }

}
