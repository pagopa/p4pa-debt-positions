package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.classification.dto.generated.CalculateAmountBalanceRequest;
import it.gov.pagopa.pu.classification.dto.generated.DebtPositionTypeOrgBalanceCostDTO;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgBalanceCostRepository;
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
  private final DebtPositionTypeOrgBalanceCostRepository debtPositionTypeOrgBalanceCostRepository;

  public BalanceResolverService(
    BalanceService balanceService,
    OrganizationService organizationService,
    DebtPositionTypeOrgBalanceCostRepository debtPositionTypeOrgBalanceCostRepository
  ) {
    this.balanceService = balanceService;
    this.organizationService = organizationService;
    this.debtPositionTypeOrgBalanceCostRepository = debtPositionTypeOrgBalanceCostRepository;
  }

  public String getBalanceDefault(Long organizationId, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    log.info("Retrieving balance from DebtPositionTypeOrg with orgId[{}] and debtPositionTypeCode[{}]", organizationId, debtPositionTypeOrg.getCode());

    if (StringUtils.isNotBlank(debtPositionTypeOrg.getBalance())) {
      return debtPositionTypeOrg.getBalance();
    }

    return balanceService.getBalanceByAssessmentRegistry(organizationId, debtPositionTypeOrg.getCode(), accessToken);
  }

  private String resolveAmountBalance(Long organizationId, Long debtPositionTypeOrgId, InstallmentNoPII installment, String accessToken) {
    Organization org = organizationService.getOrganizationById(organizationId, accessToken)
      .orElseThrow(() -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_ORGANIZATION_NOT_FOUND, "Organization with id " + organizationId + " not found"));

    Long notificationFeeCents = installment.getNotificationFeeCents();
    boolean hasNotificationFee = notificationFeeCents != null && notificationFeeCents > 0;

    DebtPositionTypeOrgBalanceCostDTO debtPositionTypeOrgBalanceCostDTO = hasNotificationFee
      ? fetchDebtPositionTypeOrgBalanceCostDTO(debtPositionTypeOrgId)
      : null;

    long totalAmountCentsPrimaryOrg = installment.getTransfers().stream()
      .filter(transfer -> transfer.getOrgFiscalCode().equals(org.getOrgFiscalCode()))
      .mapToLong(Transfer::getAmountCents).sum();

    if (hasNotificationFee) {
      totalAmountCentsPrimaryOrg -= notificationFeeCents;
    }

    CalculateAmountBalanceRequest amountBalanceRequest = CalculateAmountBalanceRequest.builder()
      .balance(installment.getBalance())
      .amountCents(totalAmountCentsPrimaryOrg)
      .remittanceInformation(installment.getRemittanceInformation())
      .notificationFeeCents(notificationFeeCents)
      .debtPositionTypeOrgBalanceCost(debtPositionTypeOrgBalanceCostDTO)
      .build();

    return balanceService.calculateAmountBalance(amountBalanceRequest, accessToken);
  }

  private DebtPositionTypeOrgBalanceCostDTO fetchDebtPositionTypeOrgBalanceCostDTO(Long debtPositionTypeOrgId) {
    return debtPositionTypeOrgBalanceCostRepository.findById(debtPositionTypeOrgId)
      .map(debtPositionTypeOrgBalanceCost -> DebtPositionTypeOrgBalanceCostDTO.builder()
        .assessmentCode(debtPositionTypeOrgBalanceCost.getAssessmentCode())
        .officeCode(debtPositionTypeOrgBalanceCost.getOfficeCode())
        .sectionCode(debtPositionTypeOrgBalanceCost.getSectionCode())
        .build())
      .orElse(null);
  }

  public void updateBalanceResolvingAmount(InstallmentNoPII installment, Long organizationId, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
    if (StringUtils.isBlank(installment.getBalance())) {
      String balance = getBalanceDefault(organizationId, debtPositionTypeOrg, accessToken);
      installment.setBalance(balance);
    }

    if (StringUtils.isNotBlank(installment.getBalance())) {
      String balanceResolved = resolveAmountBalance(organizationId, debtPositionTypeOrg.getDebtPositionTypeOrgId(), installment, accessToken);
      installment.setBalance(balanceResolved);
    }
  }
}
