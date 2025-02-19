package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GenerateIuvServiceImpl implements GenerateIuvService {

  private final IuvService iuvService;

  public GenerateIuvServiceImpl(IuvService iuvService){
    this.iuvService = iuvService;
  }


  @Override
  public String generateIuv(Organization org) {
    String iuv = iuvService.generateIuv(org);
    log.debug("generated new IUV[{}] for organization[{}/{}]", iuv, org.getIpaCode(), org.getOrgFiscalCode());
    return iuv;
  }

  @Override
  public String iuv2Nav(String iuv) {
    return iuvService.iuv2Nav(iuv);
  }
}
