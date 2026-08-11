package it.gov.pagopa.pu.common.pii.citizen.service;

import it.gov.pagopa.pu.debtpositions.exception.common.IllegalStateBusinessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  void givenJsonSerializationExceptionWhenEncryptObjThenThrowIllegalStateBusinessException() {
    JsonMapper jsonMapperMock = mock(JsonMapper.class);

    when(jsonMapperMock.writeValueAsString(any())).thenThrow(mock(JacksonException.class));

    DataCipherService brokenServiceWithMock = new DataCipherService("PSW", "PEPPER", jsonMapperMock);

    IllegalStateBusinessException ex = Assertions.assertThrows(
      IllegalStateBusinessException.class,
      () -> brokenServiceWithMock.encryptObj("PLAINTEXT")
    );

    assertEquals("JSON_SERIALIZATION_ERROR",ex.getCode());
    assertEquals("Cannot serialize object as JSON", ex.getMessage());
  }

  @Test
  void givenJsonDeserializationExceptionWhenDecryptObjThenThrowIllegalStateBusinessException() {
    JsonMapper jsonMapperMock = mock(JsonMapper.class);

    when(jsonMapperMock.readValue(any(String.class), Mockito.eq(String.class)))
      .thenThrow(mock(JacksonException.class));

    DataCipherService brokenServiceWithMock = new DataCipherService("PSW", "PEPPER", jsonMapperMock);

    byte[] validCipher = service.encryptObj("PLAINTEXT");

    IllegalStateBusinessException ex = Assertions.assertThrows(
      IllegalStateBusinessException.class,
      () -> brokenServiceWithMock.decryptObj(validCipher, String.class)
    );

    assertEquals("JSON_DESERIALIZATION_ERROR",ex.getCode());
    assertEquals("Cannot deserialize object as JSON", ex.getMessage());
  }
}
