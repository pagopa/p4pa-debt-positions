package it.gov.pagopa.pu.debtpositions.connector.workflow.config;

import it.gov.pagopa.pu.debtpositions.connector.config.ClientConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rest.workflow-hub")
@SuperBuilder
@NoArgsConstructor
public class WorkflowClientConfig extends ClientConfig {
}
