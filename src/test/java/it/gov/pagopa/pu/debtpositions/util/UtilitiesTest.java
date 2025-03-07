package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesTest {

  @Test
  void testIbanInvalid(){
    String iban = "test";
    boolean result = Utilities.isValidIban(iban);
    assertFalse(result);
  }

  @Test
  void testLocalDatetimeToOffsetDateTime() {
    OffsetDateTime expectedOffsetDateTime = OffsetDateTime.now();

    OffsetDateTime result = Utilities.localDatetimeToOffsetDateTime(expectedOffsetDateTime.toLocalDateTime());

    assertEquals(expectedOffsetDateTime, result);
    }

  @ParameterizedTest
  @ValueSource(strings = {"", "12345", "12345abc123", "1234/abc123"})
  void testValidateEmptyPIVA(String piva){
    boolean result = Utilities.isValidPIVA(piva);
    assertFalse(result);
  }

  @Test
  void testLocalDatetimeToOffsetDateTimeWithNull() {
    assertNull(Utilities.localDatetimeToOffsetDateTime(null), "The result should be null for a null input.");
  }

  @Test
  void testGetRandomIUD() {
    String iud = Utilities.getRandomIUD();

    assertTrue(iud.startsWith("000"));
  }

  @Test
  void testGetRandomicUUID() {
    String uuid = Utilities.getRandomicUUID();

    assertEquals(32, uuid.length());
  }

  @Test
  void testGenerateRandomIupd() {
    String uuid = Utilities.generateRandomIupd("60206350377");

    String regex = "^60206350377-\\d{12}-[a-f0-9]{10}$";

    assertTrue(uuid.matches(regex));
  }

  @ParameterizedTest
  @MethodSource("valueSource")
  void testCalculateIntervalBetweenOffsetDateTime(OffsetDateTime dateFrom, OffsetDateTime dateTo, ChronoUnit chronoUnit, Long interval){

    long result = Utilities.calculateIntervalBetweenOffsetDateTime(dateFrom, dateTo, chronoUnit);

    assertEquals(interval, result);
  }

  static Stream<Arguments> valueSource() {
    OffsetDateTime now = OffsetDateTime.now();
    return Stream.of(
      Arguments.of(now, now.plusMinutes(24), ChronoUnit.MINUTES, 24L),
      Arguments.of(now, now.plusHours(20), ChronoUnit.HOURS, 20L),
      Arguments.of(now, now.plusDays(60), ChronoUnit.DAYS, 60L),
      Arguments.of(now, now.plusWeeks(4), ChronoUnit.WEEKS, 4L),
      Arguments.of(now, now.plusMonths(5), ChronoUnit.MONTHS, 5L),
      Arguments.of(now, now.plusYears(3), ChronoUnit.YEARS,3L)
    );
  }
}
