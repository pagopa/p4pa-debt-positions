package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class TaxonomyValidatorServiceImpl implements TaxonomyValidatorService {

  private final TaxonomyService taxonomyService;
  private final OrganizationService organizationService;

  public TaxonomyValidatorServiceImpl(TaxonomyService taxonomyService, OrganizationService organizationService) {
    this.taxonomyService = taxonomyService;
    this.organizationService = organizationService;
  }

  public boolean validateTaxonomyCategory(String taxonomyCategory, String orgFiscalCode) {
    String orgTypeCode = organizationService.getOrganizationByFiscalCode(orgFiscalCode, SecurityUtils.getAccessToken())
      .map(Organization::getOrgTypeCode)
      .orElse(null);
    if (!isTaxonomyCategoryValid(taxonomyCategory, orgTypeCode)) {
      log.error("Taxonomy category is not valid");
      return false;
    }
    return true;
  }

  public boolean isTaxonomyCodeValid(String taxonomyCode, String orgTypeCode) {
      if(!taxonomyCode.startsWith("9/") || !taxonomyCode.endsWith("/")) {
        log.error("The taxonomy code [{}] does not meet the required format", taxonomyCode);
        return false;
      }
      String taxonomyCategory = Utilities.taxonomyCodeToTransferCategory(taxonomyCode);
      return isTaxonomyCategoryValid(taxonomyCategory, orgTypeCode);
  }

  public boolean isTaxonomyCategoryValid(String taxonomyCategory, String orgTypeCode) {
    String formattedTaxonomyCategory = getTaxonomyCategory(taxonomyCategory);

    try {
      String organizationType = formattedTaxonomyCategory.substring(0, 2);
      String macroAreaCode = formattedTaxonomyCategory.substring(2, 4);
      String serviceTypeCode = formattedTaxonomyCategory.substring(4, 7);
      String collectionReason = formattedTaxonomyCategory.substring(7, 9);

      if (orgTypeCode != null && !orgTypeCode.equals(organizationType)) {
        log.error("The taxonomy category code [{}] is not valid for the organization type [{}]", taxonomyCategory, orgTypeCode);
        return false;
      }

      PagedModelTaxonomy pagedModelTaxonomy = taxonomyService.getTaxonomies(
        organizationType,
        macroAreaCode,
        serviceTypeCode,
        collectionReason,
        0, 5, null,
        SecurityUtils.getAccessToken()
      );

      if (pagedModelTaxonomy == null || pagedModelTaxonomy.getEmbedded() == null || CollectionUtils.isEmpty(pagedModelTaxonomy.getEmbedded().getTaxonomies())) {
        log.error("The taxonomy category code [{}] does not exist in the archive", taxonomyCategory);
        return false;
      }
    } catch (IndexOutOfBoundsException exception) {
      log.error("The taxonomy category code [{}] does not meet the required length or format", taxonomyCategory);
      return false;
    }
    return true;
  }

  private String getTaxonomyCategory(String taxonomyCategory) {
    boolean isTaxonomyCodeFormat = taxonomyCategory.startsWith("9/") && taxonomyCategory.endsWith("/");
    return isTaxonomyCodeFormat ?
      Utilities.taxonomyCodeToTransferCategory(taxonomyCategory)
      : taxonomyCategory;
  }

}
