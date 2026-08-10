package it.gov.pagopa.pu.debtpositions.connector.workflow.config;

import it.gov.pagopa.pu.debtpositions.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.workflowhub.client.generated.DebtPositionApi;
import it.gov.pagopa.pu.workflowhub.client.generated.WorkflowApi;
import it.gov.pagopa.pu.workflowhub.client.generated.WorkflowTypeOrgEntityControllerApi;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowErrorDTO;
import it.gov.pagopa.pu.workflowhub.generated.ApiClient;
import it.gov.pagopa.pu.workflowhub.generated.BaseApi;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class WorkflowApisHolder {

  private final DebtPositionApi debtPositionApi;

  private final WorkflowApi workflowApi;

  private final WorkflowTypeOrgEntityControllerApi workflowTypeOrgEntityControllerApi;

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  public WorkflowApisHolder(
    WorkflowApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "WORKFLOW-HUB", clientConfig.isPrintBodyWhenError(),
      WorkflowErrorDTO.class, WorkflowErrorDTO::getCode, WorkflowErrorDTO::getMessage)
    );

    this.debtPositionApi = new DebtPositionApi(apiClient);
    this.workflowApi = new WorkflowApi(apiClient);
    this.workflowTypeOrgEntityControllerApi = new WorkflowTypeOrgEntityControllerApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public DebtPositionApi getDebtPositionApi(String accessToken) {
    return getApi(accessToken, debtPositionApi);
  }

  public WorkflowApi getWorkflowApi(String accessToken) {
    return getApi(accessToken, workflowApi);
  }

  public WorkflowTypeOrgEntityControllerApi getWorkflowTypeOrgEntityControllerApi(String accessToken) {
    return getApi(accessToken, workflowTypeOrgEntityControllerApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
