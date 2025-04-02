package it.gov.pagopa.pu.debtpositions.repository.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class BalanceValidatorImpl implements ConstraintValidator<BalanceValidator, String> {

  private final BalanceService balanceService;

  public BalanceValidatorImpl(BalanceService balanceService) {
    this.balanceService = balanceService;
  }

  @Override
  public boolean isValid(String balance, ConstraintValidatorContext context) {
    return StringUtils.isBlank(balance) ||
      BooleanUtils.isTrue(balanceService.isValidBalance(balance, SecurityUtils.getAccessToken()));
  }
}

