package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaxonomyCodeConstraintValidator implements ConstraintValidator<TaxonomyCodeConstraint, String> {

  private final TaxonomyValidatorService taxonomyValidatorService;

  public TaxonomyCodeConstraintValidator(TaxonomyValidatorService taxonomyValidatorService) {
    this.taxonomyValidatorService = taxonomyValidatorService;
  }

  @Override
  public boolean isValid(String taxonomyCode, ConstraintValidatorContext context) {
    return taxonomyValidatorService.isTaxonomyCodeValid(taxonomyCode);
  }
}
