package it.gov.pagopa.pu.debtpositions.connector.migration.service;

import it.gov.pagopa.pu.debtpositions.connector.auth.AuthnService;
import it.gov.pagopa.pu.debtpositions.connector.migration.client.DebtPositionTypeOrgOperatorsClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtPositionTypeOrgOperatorsServiceImpl implements DebtPositionTypeOrgOperatorsService {
  private final AuthnService authnService;
  private final DebtPositionTypeOrgOperatorsClient debtPositionTypeOrgOperatorsClient;

  public DebtPositionTypeOrgOperatorsServiceImpl(AuthnService authnService, DebtPositionTypeOrgOperatorsClient debtPositionTypeOrgOperatorsClient) {
    this.authnService = authnService;
    this.debtPositionTypeOrgOperatorsClient = debtPositionTypeOrgOperatorsClient;
  }

  @Override
  public List<Long> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode, String orgIpaCode) {
    return debtPositionTypeOrgOperatorsClient.getUnconsumedDebtPositionTypeOrgIds(organizationId,fiscalCode,authnService.getAccessToken(orgIpaCode));
  }

  @Override
  public void consumeDebtPositionTypeOrgOperators(Long organizationId, List<Long> debtPositionTypeOrgIds, String fiscalCode, String orgIpaCode) {
    debtPositionTypeOrgOperatorsClient.consumeDebtPositionTypeOrgOperators(organizationId, debtPositionTypeOrgIds, fiscalCode, authnService.getAccessToken(orgIpaCode));
  }
}
