package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.organization.dto.generated.Organization;

/**
 * Service class responsible for generating a valid and unique IUV for a given organization.
 * Valid means that can be used to generate a notice
 */
public interface GenerateIuvService {

  /**
   * Generate a valid and unique IUV given the fiscal code of the organization.
   * @param org the organization for which the IUV is requested
   * @return the IUV generated
   */
  String generateIuv(Organization org, String accessToken);

  String iuv2Nav(String iuv);
}
