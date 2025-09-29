package it.gov.pagopa.pu.debtpositions.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TaxonomyCodeConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaxonomyCodeConstraint {

  String message() default "The taxonomy code is not valid";
  Class<?>[] groups() default { };
  Class<? extends Payload>[] payload() default { };
}
