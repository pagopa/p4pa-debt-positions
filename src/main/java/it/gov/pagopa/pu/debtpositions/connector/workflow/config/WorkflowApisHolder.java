package it.gov.pagopa.pu.debtpositions.connector.workflow.config;

import it.gov.pagopa.pu.debtpositions.config.RestTemplateConfig;
import it.gov.pagopa.pu.workflowhub.controller.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflowhub.generated.ApiClient;
import it.gov.pagopa.pu.workflowhub.generated.BaseApi;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WorkflowApisHolder {

  private final DebtPositionApi debtPositionApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public WorkflowApisHolder(
    WorkflowClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    if (clientConfig.isPrintBodyWhenError()) {
      restTemplate.setErrorHandler(RestTemplateConfig.bodyPrinterWhenError("WORKFLOW-HUB"));
    }

    this.debtPositionApi = new DebtPositionApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public DebtPositionApi getDebtPositionApi(String accessToken) {
    return getApi(accessToken, debtPositionApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
