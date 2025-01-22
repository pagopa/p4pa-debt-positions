package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgOperators;

public class DebtPositionTypeOrgOperatorsFaker {

  public static DebtPositionTypeOrgOperators buildDebtPositionTypeOrgOperators() {
    DebtPositionTypeOrgOperators dpo = new DebtPositionTypeOrgOperators();
    dpo.setDebtPositionTypeOrgId(1L);
    dpo.setDebtPositionTypeOrgOperatorId(2L);
    dpo.setOperatorExternalUserId("user");
    return dpo;
  }
}
