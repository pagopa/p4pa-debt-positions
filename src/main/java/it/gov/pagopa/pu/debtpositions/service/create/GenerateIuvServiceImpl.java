package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.exception.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.connector.organization.OrganizationService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GenerateIuvServiceImpl implements GenerateIuvService {

  private final OrganizationService organizationService;

  private final IuvService iuvService;

  public GenerateIuvServiceImpl(OrganizationService organizationService, IuvService iuvService){
    this.organizationService = organizationService;
    this.iuvService = iuvService;
  }


  @Override
  public String generateIuv(String orgFiscalCode, String accessToken) {
    if(StringUtils.isBlank(orgFiscalCode)){
      throw new InvalidValueException("invalid orgFiscalCode");
    }
    Organization org = organizationService.getOrganizationByFiscalCode(orgFiscalCode, accessToken)
      .orElseThrow(() -> new InvalidValueException("invalid organization"));

    String iuv = iuvService.generateIuv(org);
    log.debug("generated new IUV[{}] for organization[{}/{}]", iuv, org.getIpaCode(), org.getOrgFiscalCode());
    return iuv;
  }
}
