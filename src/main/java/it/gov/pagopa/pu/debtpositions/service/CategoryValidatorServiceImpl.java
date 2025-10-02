package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class CategoryValidatorServiceImpl implements CategoryValidatorService {

  private final TaxonomyService taxonomyService;

  public CategoryValidatorServiceImpl(TaxonomyService taxonomyService) {
    this.taxonomyService = taxonomyService;
  }

  public boolean isTaxonomyCodeValid(String taxonomyCode) {
      if(!taxonomyCode.startsWith("9/") || !taxonomyCode.endsWith("/")) {
        log.error("The taxonomy code [" + taxonomyCode + "] does not meet the required format");
        return false;
      }
      String category = Utilities.taxonomyCodeToTransferCategory(taxonomyCode);
      return isCategoryValid(category);
  }

  public boolean isCategoryValid(String category) {
    try {
      String organizationType = category.substring(0, 2);
      String macroAreaCode = category.substring(2, 4);
      String serviceTypeCode = category.substring(4, 7);
      String collectionReason = category.substring(7, 9);

      PagedModelTaxonomy pagedModelTaxonomy = taxonomyService.getTaxonomies(
        organizationType,
        macroAreaCode,
        serviceTypeCode,
        collectionReason,
        0, 5, null,
        SecurityUtils.getAccessToken()
      );

      if (pagedModelTaxonomy == null || pagedModelTaxonomy.getEmbedded() == null || CollectionUtils.isEmpty(pagedModelTaxonomy.getEmbedded().getTaxonomies())) {
        log.error("The category code [" + category + "] does not exist in the archive");
        return false;
      }
    } catch (IndexOutOfBoundsException exception) {
      log.error("The category code [" + category + "] does not meet the required length or format");
      return false;
    }
    return true;
  }

}
