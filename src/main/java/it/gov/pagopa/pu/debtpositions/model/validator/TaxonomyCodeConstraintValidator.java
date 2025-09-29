package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Slf4j
public class TaxonomyCodeConstraintValidator implements ConstraintValidator<TaxonomyCodeConstraint, String> {

  private final TaxonomyService taxonomyService;

  public TaxonomyCodeConstraintValidator(TaxonomyService taxonomyService) {
    this.taxonomyService = taxonomyService;
  }

  @Override
  public boolean isValid(String taxonomyCode, ConstraintValidatorContext context) {
    try {
      String taxonomyCodeWithoutPrefix = taxonomyCode.substring(taxonomyCode.indexOf("/") + 1);

      String organizationType = taxonomyCodeWithoutPrefix.substring(0, 2);
      String macroAreaCode = taxonomyCodeWithoutPrefix.substring(2, 4);
      String serviceTypeCode = taxonomyCodeWithoutPrefix.substring(4, 7);
      String collectionReason = taxonomyCodeWithoutPrefix.substring(7, 9);
      PagedModelTaxonomy pagedModelTaxonomy = taxonomyService.getTaxonomies(
        organizationType,
        macroAreaCode,
        serviceTypeCode,
        collectionReason,
        0, 5, null,
        SecurityUtils.getAccessToken()
      );

      if (pagedModelTaxonomy == null || pagedModelTaxonomy.getEmbedded() == null || CollectionUtils.isEmpty(pagedModelTaxonomy.getEmbedded().getTaxonomies())) {
        log.error("The category code " + taxonomyCode + " does not exist in the archive");
        return false;
      }
    } catch (IndexOutOfBoundsException exception) {
      log.error("The category code " + taxonomyCode + " does not meet the required length or format");
      return false;
    }
    return true;
  }
}
