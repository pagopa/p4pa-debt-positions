package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.debtpositions.connector.classification.client.BalanceClient;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl implements BalanceService {

  private final BalanceClient balanceClient;

  public BalanceServiceImpl(BalanceClient balanceClient) {
    this.balanceClient = balanceClient;
  }

  @Override
  public Boolean validateBalance(String balance, String accessToken) {
    return balanceClient.validateBalance(balance, accessToken);
  }
}
