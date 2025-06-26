package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class UtilitiesTest {

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
  @ValueSource(strings = {"", "12345", "12345abc123", "1234/abc123", "00000000001"})
  void testValidateEmptyPIVA(String piva){
    boolean result = Utilities.isValidPIVA(piva, true);
    assertFalse(result);
  }

  @Test
  void testValidateFiscalCode(){
    boolean result = Utilities.isValidFiscalCode("AAAAAA");
    assertFalse(result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAAAAA", "000001"})
  void testValidateFiscalCodeOrPiva(String fiscalCode){
    boolean result = Utilities.isValidFiscalCodeOrPIVA(fiscalCode, false);
    assertFalse(result);
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAAAAA00B00C000D", "01234567890"})
  void testPositiveValidateFiscalCodeOrPiva(String fiscalCode){
    boolean result = Utilities.isValidFiscalCodeOrPIVA(fiscalCode, false);
    assertTrue(result);
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
  void testIsValidIntervalBetweenOffsetDateTime(OffsetDateTime dateFrom, OffsetDateTime dateTo, ChronoUnit chronoUnit, Long maxInterval,Boolean expectedResult){

    boolean result = Utilities.isValidIntervalBetweenOffsetDateTime(dateFrom, dateTo, chronoUnit, maxInterval);

    assertEquals(expectedResult, result);
  }

  static Stream<Arguments> valueSource() {
    OffsetDateTime now = OffsetDateTime.now();
    return Stream.of(
      Arguments.of(now, now.plusMinutes(24), ChronoUnit.MINUTES, 24L, true),
      Arguments.of(now, now.plusHours(20), ChronoUnit.HOURS, 20L, true),
      Arguments.of(now, now.plusDays(60), ChronoUnit.DAYS, 60L, true),
      Arguments.of(now, now.plusWeeks(4), ChronoUnit.WEEKS, 4L, true),
      Arguments.of(now, now.plusMonths(5), ChronoUnit.MONTHS, 5L, true),
      Arguments.of(now, now.plusYears(3), ChronoUnit.YEARS,3L, true),
      Arguments.of(now, now.plusHours(20), ChronoUnit.HOURS, 10L, false),
      Arguments.of(now, now.plusDays(60), ChronoUnit.DAYS, 30L, false),
      Arguments.of(now, now.plusWeeks(4), ChronoUnit.WEEKS, 3L, false),
      Arguments.of(now, now.plusMonths(5), ChronoUnit.MONTHS, 2L, false),
      Arguments.of(now, now.plusYears(3), ChronoUnit.YEARS,2L, false)
    );
  }

  @Test
  void testCheckImmutableField_OffsetDateTime(){
    List<String> result = new ArrayList<>();
    OffsetDateTime o1 = OffsetDateTime.now();
    OffsetDateTime o2 = o1.withOffsetSameInstant(ZoneOffset.MIN);
    Utilities.checkImmutableField("fieldName", o1, o2, result);

    Utilities.checkImmutableField("expectedDiffer", o1, o2.minusSeconds(1), result);

    Assertions.assertEquals(List.of("expectedDiffer"), result);
  }

  @Test
  void testCheckImmutableField_Comparable(){
    List<String> result = new ArrayList<>();
    BigDecimal o1 = BigDecimal.ONE;
    BigDecimal o2 = BigDecimal.valueOf(1_00, 2);
    Utilities.checkImmutableField("fieldName", o1, o2, result);

    Utilities.checkImmutableField("expectedDiffer", o1, o2.add(BigDecimal.ONE), result);

    Assertions.assertEquals(List.of("expectedDiffer"), result);
  }

  @Test
  void testCheckImmutableField_Object(){
    List<String> result = new ArrayList<>();
    String o1 = "string";
    String o2 = "string";
    Utilities.checkImmutableField("fieldName", o1, o2, result);

    Utilities.checkImmutableField("expectedDiffer", o1, o2.concat("1"), result);

    Assertions.assertEquals(List.of("expectedDiffer"), result);
  }

  @Test
  void testGetTraceId(){
    // Given
    String expectedResult = "TRACEID";
    setTraceId(expectedResult);

    // When
    String result = Utilities.getTraceId();

    // Then
    Assertions.assertSame(expectedResult, result);
    clearTraceIdContext();
  }

  @Test
  void givenValidLongWhenLongCentsToBigDecimalEuroThenOk() {
    // Given
    Long centsAmount = 7500L;
    BigDecimal expectedEuro = new BigDecimal("75.00");

    // When
    BigDecimal result = Utilities.longCentsToBigDecimalEuro(centsAmount);

    // Then
    assertEquals(expectedEuro, result);
  }

  @Test
  void givenNullWhenLongCentsToBigDecimalEuroThenNull() {
    // Given
    Long centsAmount = null;
    // When & Then
    assertNull(Utilities.longCentsToBigDecimalEuro(centsAmount));
  }

  public static void setTraceId(String traceId) {
    MDC.put("traceId", traceId);
  }
  public static void clearTraceIdContext(){
    MDC.clear();
  }
}
