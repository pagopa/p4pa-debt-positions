package it.gov.pagopa.pu.debtpositions.repository.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomValidatorFactory implements ConstraintValidatorFactory {

  private final BalanceService balanceService;

  public CustomValidatorFactory(BalanceService balanceService) {
    this.balanceService = balanceService;
  }

  @Override
  public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
    if (key == BalanceValidatorImpl.class) {
      return (T) new BalanceValidatorImpl(balanceService);
    }
    try {
      return key.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Cannot create validator: " + key, e);
    }
  }

  @Override
  public void releaseInstance(ConstraintValidator<?, ?> instance) {
    // no-op
  }
}

