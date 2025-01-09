package it.gov.pagopa.pu.debtpositions.citizen.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

class DataCipherServiceTest {

  private final DataCipherService service = new DataCipherService("PSW","PEPPER", new ObjectMapper());

  @Test
  void testEncrypt() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] cipher = service.encrypt(plain);
    String result = service.decrypt(cipher);

    // Then
    Assertions.assertEquals(plain, result);
  }

  @Test
  void testEncryptObj() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] cipher = service.encryptObj(plain);
    String result = service.decryptObj(cipher, String.class);

    // Then
    Assertions.assertEquals(plain, result);
  }

  @Test
  void testHash() {
    // Given
    String plain = "PLAINTEXT";

    // When
    byte[] hash = service.hash(plain);

    // Then
    Assertions.assertEquals("s+QUCtO7vYNzHCDrH03EVRGPZTyfIXwBKTRrgYWqwc4=", Base64.getEncoder().encodeToString(hash));
  }

  @Test
  void testHashNull() {
    // When
    byte[] hash = service.hash(null);

    // Then
    Assertions.assertNull(hash);
  }
}
