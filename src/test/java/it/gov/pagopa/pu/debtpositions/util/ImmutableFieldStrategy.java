package it.gov.pagopa.pu.debtpositions.util;

import uk.co.jemos.podam.common.AttributeStrategy;

import java.lang.annotation.Annotation;
import java.util.List;

public class ImmutableFieldStrategy implements AttributeStrategy<Object> {

  @Override
  public Object getValue(Class<?> aClass, List<Annotation> list) {
    return "defaultImmutableValue";
  }
}
