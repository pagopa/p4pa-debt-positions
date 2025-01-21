package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilitiesTest {

  @Test
  void testIbanInvalid(){
    String iban = "test";
    boolean result = Utilities.isValidIban(iban);
    assertFalse(result);
  }

  @Test
  void testLocalDatetimeToOffsetDateTime() {
    LocalDateTime localDateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    OffsetDateTime expectedOffsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);

    OffsetDateTime result = Utilities.localDatetimeToOffsetDateTime(localDateTime);

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
    OffsetDateTime result = Utilities.localDatetimeToOffsetDateTime(null);

    assertNull(result, "The result should be null for a null input.");
  }
}
