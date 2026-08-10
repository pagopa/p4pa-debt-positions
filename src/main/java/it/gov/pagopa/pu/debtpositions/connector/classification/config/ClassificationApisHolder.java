package it.gov.pagopa.pu.debtpositions.connector.classification.config;

import it.gov.pagopa.pu.classification.client.generated.BalanceApi;
import it.gov.pagopa.pu.classification.dto.generated.ClassificationErrorDTO;
import it.gov.pagopa.pu.classification.generated.ApiClient;
import it.gov.pagopa.pu.classification.generated.BaseApi;
import it.gov.pagopa.pu.debtpositions.config.rest.HttpClientErrorJsonBodyHandler;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ClassificationApisHolder {

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  private final BalanceApi balanceApi;

  public ClassificationApisHolder (
    ClassificationApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "CLASSIFICATION", clientConfig.isPrintBodyWhenError(),
      ClassificationErrorDTO.class, ClassificationErrorDTO::getCode, ClassificationErrorDTO::getMessage)
    );

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
