package it.gov.pagopa.pu.debtpositions.connector.classification.client;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.classification.dto.generated.ValidateBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.config.ClassificationApisHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
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
    try {
      return classificationApisHolder.getBalanceApi(accessToken).getBalanceByAssessmentRegistry(organizationId, debtPositionTypeOrgCode);
    } catch (HttpClientErrorException.NotFound e){
      log.info("Cannot find assessment registry associated to organizationId {} and debt position type org {}", organizationId, debtPositionTypeOrgCode);
      return null;
    }
  }

  public String calculateAmountBalance(CalculateAmountBalanceRequest request, String accessToken) {
    return classificationApisHolder.getBalanceApi(accessToken).calculateAmountBalance(request);
  }
}
