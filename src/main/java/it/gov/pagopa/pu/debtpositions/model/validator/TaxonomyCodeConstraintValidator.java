package it.gov.pagopa.pu.debtpositions.model.validator;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaxonomyCodeConstraintValidator implements ConstraintValidator<TaxonomyCodeConstraint, DebtPositionType> {

  private final TaxonomyValidatorService taxonomyValidatorService;

  public TaxonomyCodeConstraintValidator(TaxonomyValidatorService taxonomyValidatorService) {
    this.taxonomyValidatorService = taxonomyValidatorService;
  }

  @Override
  public boolean isValid(DebtPositionType debtPositionType, ConstraintValidatorContext context) {
    String taxonomyCode = debtPositionType.getTaxonomyCode();
    String orgType = debtPositionType.getOrgType();

    return taxonomyValidatorService.isTaxonomyCodeValid(taxonomyCode, orgType);
  }
}
