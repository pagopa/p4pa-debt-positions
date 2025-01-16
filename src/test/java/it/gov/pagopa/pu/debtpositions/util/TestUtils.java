package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {
  private TestUtils(){}

  public static final LocalDateTime LOCALDATETIME = LocalDateTime.of(2024, 5, 15, 10, 30, 0);
  public static final OffsetDateTime OFFSETDATETIME = ZonedDateTime.of(LOCALDATETIME, ZoneId.of("Europe/Rome")).toOffsetDateTime();
  public static final Date DATE = Date.from(LOCALDATETIME
    .atZone(ZoneId.systemDefault())
    .toInstant()
  );

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
