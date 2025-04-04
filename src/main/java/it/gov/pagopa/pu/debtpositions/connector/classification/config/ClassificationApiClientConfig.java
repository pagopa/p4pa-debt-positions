package it.gov.pagopa.pu.debtpositions.connector.classification.config;

import it.gov.pagopa.pu.debtpositions.config.ApiClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.classification")
@SuperBuilder
@NoArgsConstructor
public class ClassificationApiClientConfig extends ApiClientConfig {
}
