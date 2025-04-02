package it.gov.pagopa.pu.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.repository.validator.CustomValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class AppConfig {

  private final CustomValidatorFactory customValidatorFactory;

  public AppConfig(CustomValidatorFactory customValidatorFactory) {
    this.customValidatorFactory = customValidatorFactory;
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
    factory.setConstraintValidatorFactory(customValidatorFactory);
    return factory;
  }
}

