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
  Boolean validateBalance(String balance, String accessToken);
}
