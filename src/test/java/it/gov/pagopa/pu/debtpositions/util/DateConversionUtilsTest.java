package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

class DateConversionUtilsTest {

  @Test
  void testOffsetDateTime2LocalDateTime() {
    // Given
    OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDate.of(2025, 1, 1), LocalTime.of(0,0), ZoneOffset.UTC);
    LocalDateTime expectedResult = LocalDateTime.of(LocalDate.of(2025, 1, 1), LocalTime.of(1, 0));

    // When
    LocalDateTime result = DateConversionUtils.offsetDateTime2LocalDateTime(offsetDateTime);

    // Then
    Assertions.assertEquals(expectedResult, result);
  }
}
