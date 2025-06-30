package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistry;
import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.AssessmentsRegistryService;
import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Set;

@Service
@Slf4j
public class InstallmentSynchronizeFetchBalanceService {
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final AssessmentsRegistryService assessmentsRegistryService;
  private final BalanceService balanceService;

  private static final String OPERATING_YEAR = String.valueOf(LocalDate.now().getYear());
  private static final AssessmentsRegistryStatus ASSESSMENTS_REGISTRY_STATUS = AssessmentsRegistryStatus.ACTIVE;

  public InstallmentSynchronizeFetchBalanceService(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, AssessmentsRegistryService assessmentsRegistryService, BalanceService balanceService) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.assessmentsRegistryService = assessmentsRegistryService;
    this.balanceService = balanceService;
  }

  public String getDebtPositionTypeDefaultBalance(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken) {
    log.info("Retrieving balance from DebtPositionTypeOrg with orgId[{}] and debtPositionTypeCode[{}]", installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode());
    DebtPositionTypeOrg debtPositionTypeOrg = retrieveDebtPositionTypeOrg(installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode());
    if (StringUtils.isNotBlank(debtPositionTypeOrg.getBalance())) {
      return debtPositionTypeOrg.getBalance();
    }

    log.info("Retrieving balance from AssessmentsRegistry with orgId[{}], debtPositionTypeCode[{}], operatingYear [{}], status [{}]", installmentSynchronizeDTO.getOrganizationId(), installmentSynchronizeDTO.getDebtPositionTypeCode(), OPERATING_YEAR, ASSESSMENTS_REGISTRY_STATUS);
    PagedModelAssessmentsRegistry assessmentsRegistry = assessmentsRegistryService.findAssessmentsRegistriesByFilters(
      installmentSynchronizeDTO.getOrganizationId(),
      Set.of(installmentSynchronizeDTO.getDebtPositionTypeCode()),
      null, null, null, null, null, null,
      OPERATING_YEAR,
      ASSESSMENTS_REGISTRY_STATUS,
      0,
      1,
      null,
      accessToken);

    if (isAssessmentsRegistryEmpty(assessmentsRegistry)) {
      return null;
    }

    int size = assessmentsRegistry.getEmbedded().getAssessmentsRegistries().size();
    if (size > 1) {
      throw new IllegalStateException("Expected exactly one Assessments registry result, but found " + size + ".");
    }

    String balanceXml = generateXmlFromAssessmentsRegistry(assessmentsRegistry.getEmbedded().getAssessmentsRegistries().getFirst(), installmentSynchronizeDTO.getAmountCents());
    if (BooleanUtils.isNotTrue(balanceService.isValidBalance(balanceXml, accessToken))) {
      throw new InvalidValueException("Balance is not formally valid");
    }

    return balanceXml;
  }

  private boolean isAssessmentsRegistryEmpty(PagedModelAssessmentsRegistry registry) {
    return registry == null ||
      registry.getEmbedded() == null ||
      registry.getEmbedded().getAssessmentsRegistries() == null ||
      registry.getEmbedded().getAssessmentsRegistries().isEmpty();
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(
        organizationId, debtPositionTypeCode)
      .orElseThrow(() -> new InvalidValueException(String.format("The debt position type code %s is not valid for this organizationId %s", debtPositionTypeCode, organizationId)));
  }

  private String generateXmlFromAssessmentsRegistry(AssessmentsRegistry assessmentsRegistry, Long amountCents) {
    //TODO: see task https://pagopa.atlassian.net/browse/P4ADEV-3171
    StringBuilder xmlBuilder = new StringBuilder();

    xmlBuilder.append("<bilancio>");
    xmlBuilder.append("<capitolo>");
    xmlBuilder.append("<codCapitolo>")
      .append(assessmentsRegistry.getSectionCode())
      .append("</codCapitolo>");

    if (assessmentsRegistry.getOfficeCode() != null) {
      xmlBuilder.append("<codUfficio>")
        .append(assessmentsRegistry.getOfficeCode())
        .append("</codUfficio>");
    }

    xmlBuilder.append("<accertamento>");
    if (assessmentsRegistry.getAssessmentCode() != null) {
      xmlBuilder.append("<codAccertamento>")
        .append(assessmentsRegistry.getAssessmentCode())
        .append("</codAccertamento>");
    }
    xmlBuilder.append("<importo>")
      .append(Utilities.longCentsToBigDecimalEuro(amountCents))
      .append("</importo>");
    xmlBuilder.append("</accertamento>");

    xmlBuilder.append("</capitolo>");
    xmlBuilder.append("</bilancio>");

    return xmlBuilder.toString();
  }
}
