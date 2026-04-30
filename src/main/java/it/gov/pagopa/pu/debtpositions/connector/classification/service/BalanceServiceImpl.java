package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.client.BalanceClient;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl implements BalanceService {

  private final BalanceClient balanceClient;

  public BalanceServiceImpl(BalanceClient balanceClient) {
    this.balanceClient = balanceClient;
  }

  @Override
  public Boolean isValidBalance(String balance, Long amountCents, String accessToken) {
    return balanceClient.validateBalance(balance, amountCents, accessToken);
  }

  @Override
  public String getBalanceByAssessmentRegistry(Long organizationId, String debtPositionTypeOrgCode, String accessToken) {
    return balanceClient.getBalanceByAssessmentRegistry(organizationId, debtPositionTypeOrgCode, accessToken);
  }

  @Override
  public String calculateAmountBalance(CalculateAmountBalanceRequest calculateAmountBalanceRequest, String accessToken) {
    return balanceClient.calculateAmountBalance(calculateAmountBalanceRequest, accessToken);
  }
}
