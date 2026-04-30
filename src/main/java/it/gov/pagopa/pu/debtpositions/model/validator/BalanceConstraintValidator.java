package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class BalanceConstraintValidator implements ConstraintValidator<BalanceConstraint, String> {

  private final BalanceService balanceService;

  public BalanceConstraintValidator(BalanceService balanceService) {
    this.balanceService = balanceService;
  }

  @Override
  public boolean isValid(String balance, ConstraintValidatorContext context) {
    return StringUtils.isBlank(balance) ||
      BooleanUtils.isTrue(balanceService.isValidBalance(balance, null, SecurityUtils.getAccessToken()));
  }
}
