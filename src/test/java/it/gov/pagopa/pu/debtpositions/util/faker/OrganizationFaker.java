package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.organization.dto.generated.Organization;

public class OrganizationFaker {

  public static Organization buildOrganization(){
    return TestUtils.getPodamFactory().manufacturePojo(Organization.class);
  }
}
