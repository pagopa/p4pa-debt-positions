package it.gov.pagopa.pu.debtpositions.repository.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class DebtPositionTypeOrgValidator {

  @Setter
  private static BalanceService balanceService;

  @PrePersist
  @PreUpdate
  public void validateBalance(DebtPositionTypeOrg debtPositionTypeOrg) {
    if (StringUtils.isNotBlank(debtPositionTypeOrg.getBalance()) &&
      BooleanUtils.isNotTrue(balanceService.isValidBalance(debtPositionTypeOrg.getBalance(), SecurityUtils.getAccessToken()))) {
      throw new InvalidValueException("Balance of debt position type org is not formally valid");
    }
  }
}
