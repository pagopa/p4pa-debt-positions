package it.gov.pagopa.pu.debtpositions.connector.classification.service;

/***
 * This interface provides method relating to the balance information
 */
public interface BalanceService {

  /***
   *
   * @param balance the value of balance to be validated
   * @param accessToken the access token
   * @return a Boolean based on whether the value is formally valid or not
   */
  Boolean isValidBalance(String balance, String accessToken);

  /***
   *
   * @param organizationId the id of organization
   * @param debtPositionTypeOrgCode the debt position type org code
   * @param accessToken the access token
   * @return the balance as a string
   */
  String getBalanceByAssessmentRegistry(Long organizationId, String debtPositionTypeOrgCode, String accessToken);
}
