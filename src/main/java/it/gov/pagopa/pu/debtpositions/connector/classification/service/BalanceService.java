package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;

/***
 * This interface provides method relating to the balance information
 */
public interface BalanceService {

  /***
   *
   * @param balance the value of balance to be validated
   * @param amountCents the installment's amount
   * @param accessToken the access token
   * @return a Boolean based on whether the value is formally valid or not
   */
  Boolean isValidBalance(String balance, Long amountCents, String accessToken);

  /***
   *
   * @param organizationId the id of organization
   * @param debtPositionTypeOrgCode the debt position type org code
   * @param accessToken the access token
   * @return the balance as a string
   */
  String getBalanceByAssessmentRegistry(Long organizationId, String debtPositionTypeOrgCode, String accessToken);

  /***
   *
   * @param calculateAmountBalanceRequest the request to calculate: balance with placeholder, amount of installment, remittance information
   * @param accessToken the access token
   * @return the balance with resolved amount
   */
  String calculateAmountBalance(CalculateAmountBalanceRequest calculateAmountBalanceRequest, String accessToken);
}
