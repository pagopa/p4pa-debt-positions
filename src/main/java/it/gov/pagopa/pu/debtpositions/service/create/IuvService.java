package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;

/**
 * Service class responsible for generating a valid and unique IUV for a given organization.
 * Valid means that can be used to generate a notice
 */
public interface IuvService {

  /**
   * Generate a valid and unique IUV given the fiscal code of the organization.
   * @param org the organization for which the IUV is requested
   * @param broker the broker for which the IUV is requested
   * @param segregationCode the segregation code for the related organization station
   * @return the IUV generated
   */
  String generateIuv(Organization org, Broker broker, String segregationCode);

  String iuv2Nav(String iuv);

  /**
   * Validate the syntax of the IUV that is passed by external services and returns the corresponding NAV
   * @param iuv the IUV to be validated
   * @param segregationCode the segregation code for the related organization station
   * @param broker the broker for which the IUV is requested
   * @return the NAV created
   */
  String validateIuvAndRetrieveNav(String iuv, String segregationCode, Broker broker);
}
