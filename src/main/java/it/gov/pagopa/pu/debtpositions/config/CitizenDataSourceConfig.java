package it.gov.pagopa.pu.debtpositions.config;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
  basePackages = {"it.gov.pagopa.pu.debtpositions.citizen"}
)
public class CitizenDataSourceConfig {
  @Bean
  @ConfigurationProperties("spring.datasource.citizen")
  public DataSourceProperties citizenDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name="dsCitizen")
  @ConfigurationProperties("spring.datasource.citizen")
  public DataSource citizenDataSource()  {
    return DataSourceBuilder.create().build();
  }
}
