package it.gov.pagopa.pu.debtpositions.connector.organization.config;

import it.gov.pagopa.pu.organization.client.generated.OrganizationSearchControllerApi;
import it.gov.pagopa.pu.organization.client.generated.TaxonomySearchControllerApi;
import it.gov.pagopa.pu.organization.generated.ApiClient;
import it.gov.pagopa.pu.organization.generated.BaseApi;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrganizationApisHolder {

  private final OrganizationSearchControllerApi organizationSearchControllerApi;

  private final TaxonomySearchControllerApi taxonomySearchControllerApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public OrganizationApisHolder(
    @Value("${rest.organization.base-url}") String baseUrl,

    RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(baseUrl);
    apiClient.setBearerToken(bearerTokenHolder::get);

    this.organizationSearchControllerApi = new OrganizationSearchControllerApi(apiClient);
    this.taxonomySearchControllerApi = new TaxonomySearchControllerApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  /**
   * It will return a {@link OrganizationSearchControllerApi} instrumented with the provided accessToken. Use null if auth is not required
   */
  public OrganizationSearchControllerApi getOrganizationSearchControllerApi(String accessToken) {
    return getApi(accessToken, organizationSearchControllerApi);
  }

  public TaxonomySearchControllerApi getTaxonomyCodeDtoSearchControllerApi(String accessToken) {
    return getApi(accessToken, taxonomySearchControllerApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
