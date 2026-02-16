package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStatus;

public class OrganizationFaker {

  public static Organization buildOrganization(){
    return TestUtils.getPodamFactory().manufacturePojo(Organization.class)
      .orgFiscalCode("12345678901")
      .status(OrganizationStatus.ACTIVE)
      .brokerId(1L);
  }

  public static Broker buildBroker(){
    return TestUtils.getPodamFactory().manufacturePojo(Broker.class)
      .brokerId(1L)
      .flagDelegate(Boolean.FALSE);
  }
}
