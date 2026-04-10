package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BalanceResolverService {

  private final BalanceService balanceService;
  private final OrganizationService organizationService;

  public BalanceResolverService(BalanceService balanceService, OrganizationService organizationService) {
    this.balanceService = balanceService;
    this.organizationService = organizationService;
  }

  public String getBalanceDefault(Long organizationId, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    log.info("Retrieving balance from DebtPositionTypeOrg with orgId[{}] and debtPositionTypeCode[{}]", organizationId, debtPositionTypeOrg.getCode());

    if (StringUtils.isNotBlank(debtPositionTypeOrg.getBalance())) {
      return debtPositionTypeOrg.getBalance();
    }

    return balanceService.getBalanceByAssessmentRegistry(organizationId, debtPositionTypeOrg.getCode(), accessToken);
  }

  public String resolveAmountBalance(Long organizationId, InstallmentNoPII installment, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_NOT_FOUND, "Organization with id " + organizationId + " not found"));

    Long totalAmountCentsPrimaryOrg = installment.getTransfers().stream()
      .filter(transfer -> transfer.getOrgFiscalCode().equals(org.getOrgFiscalCode()))
      .mapToLong(Transfer::getAmountCents).sum();

    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance())
      .amountCents(totalAmountCentsPrimaryOrg)
      .remittanceInformation(installment.getRemittanceInformation())
      .build();

    return balanceService.calculateAmountBalance(amountBalanceRequest, accessToken);
  }

  public void updateBalanceResolvingAmount(InstallmentNoPII installment, Long organizationId, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    if (StringUtils.isBlank(installment.getBalance())) {
      String balance = getBalanceDefault(organizationId, debtPositionTypeOrg, accessToken);
      installment.setBalance(balance);
    }

    if (StringUtils.isNotBlank(installment.getBalance())) {
      String balanceResolved = resolveAmountBalance(organizationId, installment, accessToken);
      installment.setBalance(balanceResolved);
    }
  }
}
