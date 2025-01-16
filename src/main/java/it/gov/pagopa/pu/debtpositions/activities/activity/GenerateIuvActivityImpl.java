package it.gov.pagopa.pu.debtpositions.activities.activity;

import it.gov.pagopa.pu.debtpositions.activities.exception.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.activities.connector.organization.OrganizationService;
import it.gov.pagopa.pu.debtpositions.activities.service.IuvService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@Slf4j
public class GenerateIuvActivityImpl implements GenerateIuvActivity{

  private final OrganizationService organizationService;

  private final IuvService iuvService;

  public GenerateIuvActivityImpl(OrganizationService organizationService, IuvService iuvService){
    this.organizationService = organizationService;
    this.iuvService = iuvService;
  }


  @Override
  public String generateIuv(String orgFiscalCode) {
    if(StringUtils.isBlank(orgFiscalCode)){
      throw new InvalidValueException("invalid orgFiscalCode");
    }
    Organization org = organizationService.getOrganizationByFiscalCode(orgFiscalCode)
      .orElseThrow(() -> new InvalidValueException("invalid organization"));

    String iuv = iuvService.generateIuv(org);
    log.debug("generated new IUV[{}] for organization[{}/{}]", iuv, org.getIpaCode(), org.getOrgFiscalCode());
    return iuv;
  }
}
