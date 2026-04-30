package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;

public class DebtPositionTypeFaker {

  public static DebtPositionType buildDebtPositionType(){
    DebtPositionType debtPositionType = new DebtPositionType();
    debtPositionType.setDebtPositionTypeId(100L);
    debtPositionType.setBrokerId(1L);
    debtPositionType.setCode("CODE");
    debtPositionType.setDescription("Description");
    debtPositionType.setOrgType("OrgType");
    debtPositionType.setMacroArea("MacroArea");
    debtPositionType.setServiceType("ServiceType");
    debtPositionType.setCollectingReason("CollectingReason");
    debtPositionType.setTaxonomyCode("9/001122233/");
    debtPositionType.setFlagAnonymousFiscalCode(false);
    debtPositionType.setFlagMandatoryDueDate(false);
    debtPositionType.setFlagNotifyIo(false);
    debtPositionType.setIoTemplateMessage("ioTemplateMessage");
    return debtPositionType;
  }
}
