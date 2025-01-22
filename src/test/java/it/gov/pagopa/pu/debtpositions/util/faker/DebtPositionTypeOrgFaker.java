package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;

public class DebtPositionTypeOrgFaker {

  public static DebtPositionTypeOrg buildDebtPositionTypeOrg() {
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    debtPositionTypeOrg.setDebtPositionTypeOrgId(2L);
    debtPositionTypeOrg.setDebtPositionTypeId(100L);
    debtPositionTypeOrg.setOrganizationId(500L);
    debtPositionTypeOrg.setBalance("1000.00");
    debtPositionTypeOrg.setCode("TEST_CODE");
    debtPositionTypeOrg.setDescription("Test Description");
    debtPositionTypeOrg.setIban("IT60X0542811101000000123456");
    debtPositionTypeOrg.setPostalIban("IT60X0542811101000000123457");
    debtPositionTypeOrg.setPostalAccountCode("12345678");
    debtPositionTypeOrg.setHolderPostalCc("Test Holder");
    debtPositionTypeOrg.setOrgSector("Finance");
    debtPositionTypeOrg.setXsdDefinitionRef("definition.xsd");
    debtPositionTypeOrg.setAmountCents(100L);
    debtPositionTypeOrg.setExternalPaymentUrl("https://payment.example.com");
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(true);
    debtPositionTypeOrg.setFlagMandatoryDueDate(true);
    debtPositionTypeOrg.setFlagSpontaneous(true);
    debtPositionTypeOrg.setFlagNotifyIo(true);
    debtPositionTypeOrg.setIoTemplateMessage("Test IO Template Message");
    debtPositionTypeOrg.setFlagActive(true);
    debtPositionTypeOrg.setFlagNotifyOutcomePush(false);
    debtPositionTypeOrg.setNotifyOutcomePushOrgSilServiceId(200L);
    debtPositionTypeOrg.setFlagAmountActualization(true);
    debtPositionTypeOrg.setAmountActualizationOrgSilServiceId(300L);
    debtPositionTypeOrg.setFlagExternal(false);
    return debtPositionTypeOrg;
  }

}
