package it.gov.pagopa.pu.debtpositions.citizen.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

class DataCipherServiceTest {

  private final DataCipherService service = new DataCipherService("PSW","PEPPER", new JsonMapper());

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

  @Test
  void givenJsonSerializationExceptionWhenEncryptObjThenThrowIllegalStateException() {
    JsonMapper jsonMapperMock = Mockito.mock(JsonMapper.class);

    Mockito.when(jsonMapperMock.writeValueAsString(any())).thenThrow(Mockito.mock(JacksonException.class));

    DataCipherService brokenServiceWithMock = new DataCipherService("PSW", "PEPPER", jsonMapperMock);

    IllegalStateException ex = Assertions.assertThrows(
      IllegalStateException.class,
      () -> brokenServiceWithMock.encryptObj("PLAIN")
    );

    assertEquals("[JSON_SERIALIZATION_ERROR] Cannot serialize object as JSON", ex.getMessage());
  }

  @Test
  void givenJsonDeserializationExceptionWhenDecryptObjThenThrowIllegalStateException() {
    JsonMapper jsonMapperMock = Mockito.mock(JsonMapper.class);

    Mockito.when(jsonMapperMock.readValue(any(String.class), any(Class.class)))
      .thenThrow(Mockito.mock(JacksonException.class));

    DataCipherService brokenServiceWithMock = new DataCipherService("PSW", "PEPPER", jsonMapperMock);

    byte[] validCipher = service.encryptObj("PLAIN");

    IllegalStateException ex = Assertions.assertThrows(
      IllegalStateException.class,
      () -> brokenServiceWithMock.decryptObj(validCipher, String.class)
    );

    assertEquals("[JSON_DESERIALIZATION_ERROR] Cannot deserialize object as JSON", ex.getMessage());
  }
}
