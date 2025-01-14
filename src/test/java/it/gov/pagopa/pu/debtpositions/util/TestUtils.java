package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {
  public static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2025, 1, 1, 0,0,0, 0, ZoneOffset.UTC);

  private TestUtils(){}

  /**
   * It will assert not null on all o's fields
   */
  public static void checkNotNullFields(Object o, String... excludedFields) {
    Set<String> excludedFieldsSet = new HashSet<>(Arrays.asList(excludedFields));
    org.springframework.util.ReflectionUtils.doWithFields(o.getClass(),
      f -> {
        f.setAccessible(true);
        Assertions.assertNotNull(f.get(o), "The field "+f.getName()+" of the input object of type "+o.getClass()+" is null!");
      },
      f -> !excludedFieldsSet.contains(f.getName()));
  }

}
