package it.gov.pagopa.pu.debtpositions.connector.classification.client;

import it.gov.pagopa.pu.classification.dto.generated.ValidateBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.config.ClassificationApisHolder;
import org.springframework.stereotype.Service;

@Service
public class BalanceClient {

  private final ClassificationApisHolder classificationApisHolder;

  public BalanceClient(ClassificationApisHolder classificationApisHolder) {
    this.classificationApisHolder = classificationApisHolder;
  }

  public Boolean validateBalance(String balance, String accessToken){
    ValidateBalanceRequest validateBalanceRequest = ValidateBalanceRequest.builder()
      .balance(balance).build();
    return classificationApisHolder.getBalanceApi(accessToken).validateBalance(validateBalanceRequest);
  }

  public String getBalanceByAssessmentRegistry(Long organizationId, String debtPositionTypeOrgCode, String accessToken){
    return classificationApisHolder.getBalanceApi(accessToken).getBalanceByAssessmentRegistry(organizationId, debtPositionTypeOrgCode);
  }
}
