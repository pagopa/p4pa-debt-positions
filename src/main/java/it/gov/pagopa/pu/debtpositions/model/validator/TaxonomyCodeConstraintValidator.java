package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.service.CategoryValidatorService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaxonomyCodeConstraintValidator implements ConstraintValidator<TaxonomyCodeConstraint, String> {

  private final CategoryValidatorService categoryValidatorService;

  public TaxonomyCodeConstraintValidator(CategoryValidatorService categoryValidatorService) {
    this.categoryValidatorService = categoryValidatorService;
  }

  @Override
  public boolean isValid(String taxonomyCode, ConstraintValidatorContext context) {
    return categoryValidatorService.isTaxonomyCodeValid(taxonomyCode);
  }
}
