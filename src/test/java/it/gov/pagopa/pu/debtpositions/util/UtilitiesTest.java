package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilitiesTest {

  @Test
  void testLocalDatetimeToOffsetDateTime() {
    LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    OffsetDateTime expectedOffsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);

    OffsetDateTime result = Utilities.localDatetimeToOffsetDateTime(localDateTime);

    assertEquals(expectedOffsetDateTime, result);
  }

  @Test
  void testLocalDatetimeToOffsetDateTimeWithNull() {
    OffsetDateTime result = Utilities.localDatetimeToOffsetDateTime(null);

    assertNull(result, "The result should be null for a null input.");
  }
}
