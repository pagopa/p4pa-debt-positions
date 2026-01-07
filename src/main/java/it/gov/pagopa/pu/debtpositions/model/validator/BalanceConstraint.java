package it.gov.pagopa.pu.debtpositions.model.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BalanceConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BalanceConstraint {

  String message() default "[INVALID_BALANCE] The balance is not formally valid";
  Class<?>[] groups() default { };
  Class<? extends Payload>[] payload() default { };
}
