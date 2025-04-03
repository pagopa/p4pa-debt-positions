package it.gov.pagopa.pu.debtpositions.repository.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler()
public class DebtPositionTypeOrgValidatorHandler {

  private final BalanceService balanceService;

  public DebtPositionTypeOrgValidatorHandler(BalanceService balanceService) {
    this.balanceService = balanceService;
  }

  @HandleBeforeCreate
  @HandleBeforeSave
  public void handleBeforeSaveOrCreate(DebtPositionTypeOrg debtPositionTypeOrg) {
    if(StringUtils.isNotBlank(debtPositionTypeOrg.getBalance()) &&
      BooleanUtils.isNotTrue(balanceService.isValidBalance(debtPositionTypeOrg.getBalance(), SecurityUtils.getAccessToken()))){
      throw new InvalidValueException("Balance in debt position type org is not formally valid");
    }
  }
}
