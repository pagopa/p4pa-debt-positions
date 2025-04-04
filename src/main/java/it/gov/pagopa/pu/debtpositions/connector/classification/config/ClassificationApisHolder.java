package it.gov.pagopa.pu.debtpositions.connector.classification.config;

import it.gov.pagopa.pu.classification.controller.generated.BalanceApi;
import it.gov.pagopa.pu.classification.generated.ApiClient;
import it.gov.pagopa.pu.classification.generated.BaseApi;
import it.gov.pagopa.pu.debtpositions.config.RestTemplateConfig;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ClassificationApisHolder {

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  private final BalanceApi balanceApi;

  public ClassificationApisHolder (
    ClassificationApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("CLASSIFICATION"));
    }

    this.balanceApi = new BalanceApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public BalanceApi getBalanceApi(String accessToken){
    return getApi(accessToken, balanceApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
