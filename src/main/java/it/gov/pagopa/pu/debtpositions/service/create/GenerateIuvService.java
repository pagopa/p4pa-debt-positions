package it.gov.pagopa.pu.debtpositions.service.create;

/**
 * Service class responsible for generating a valid and unique IUV for a given organization.
 * Valid means that can be used to generate a notice
 */
public interface GenerateIuvService {

  /**
   * Generate a valid and unique IUV given the fiscal code of the organization.
   * @param orgId the fiscal code of the organization for which the IUV is requested
   * @return the IUV generated
   */
  String generateIuv(Long orgId, String accessToken);

  String iuv2Nav(String iuv);
}
