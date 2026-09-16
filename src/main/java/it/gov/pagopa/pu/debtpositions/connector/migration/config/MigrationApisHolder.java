package it.gov.pagopa.pu.debtpositions.connector.migration.config;

import it.gov.pagopa.pu.debtpositions.config.rest.HttpClientErrorJsonBodyHandler;
import it.gov.pagopa.pu.debtpositions.connector.migration.mapper.MigrationErrorDTOMapper;
import it.gov.pagopa.pu.migration.client.generated.DebtPositionTypeOrgOperatorsApi;
import it.gov.pagopa.pu.migration.dto.generated.ErrorDTO;
import it.gov.pagopa.pu.migration.generated.ApiClient;
import it.gov.pagopa.pu.migration.generated.BaseApi;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class MigrationApisHolder {

  private final ThreadLocal<String> bearerTokenHolder = new ThreadLocal<>();

  private final DebtPositionTypeOrgOperatorsApi debtPositionTypeOrgOperatorsApi;

  public MigrationApisHolder (
    MigrationApiClientConfig clientConfig,
    RestTemplateBuilder restTemplateBuilder,
    JsonMapper jsonMapper
  ) {
    RestTemplate restTemplate = restTemplateBuilder.build();
    ApiClient apiClient = new ApiClient(restTemplate);
    apiClient.setBasePath(clientConfig.getBaseUrl());
    apiClient.setBearerToken(bearerTokenHolder::get);
    apiClient.setMaxAttemptsForRetry(Math.max(1, clientConfig.getMaxAttempts()));
    apiClient.setWaitTimeMillis(clientConfig.getWaitTimeMillis());
    restTemplate.setErrorHandler(new HttpClientErrorJsonBodyHandler<>(jsonMapper, "MIGRATION", clientConfig.isPrintBodyWhenError(),
      ErrorDTO.class, MigrationErrorDTOMapper::map)
    );

    this.debtPositionTypeOrgOperatorsApi = new DebtPositionTypeOrgOperatorsApi(apiClient);
  }

  @PreDestroy
  public void unload() {
    bearerTokenHolder.remove();
  }

  public DebtPositionTypeOrgOperatorsApi getDebtPositionTypeOrgOperatorsApi(String accessToken){
    return getApi(accessToken, debtPositionTypeOrgOperatorsApi);
  }

  private <T extends BaseApi> T getApi(String accessToken, T api) {
    bearerTokenHolder.set(accessToken);
    return api;
  }
}
