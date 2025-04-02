package it.gov.pagopa.pu.debtpositions.repository.validator;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionTypeOrgListenerConfig {

  private final BalanceService balanceService;

  public DebtPositionTypeOrgListenerConfig(BalanceService balanceService) {
    this.balanceService = balanceService;
  }

  @PostConstruct
  public void init(){
    DebtPositionTypeOrgValidator.setBalanceService(balanceService);
  }
}
