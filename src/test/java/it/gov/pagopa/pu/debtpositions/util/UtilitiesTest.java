package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;

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

    assertEquals(64, uuid.length());
  }
}
