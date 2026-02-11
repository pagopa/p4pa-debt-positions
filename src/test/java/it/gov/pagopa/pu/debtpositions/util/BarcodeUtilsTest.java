package it.gov.pagopa.pu.debtpositions.util;

import com.itextpdf.barcodes.Barcode128;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarcodeUtilsTest {

  private static final String BASE64_PNG_PREFIX = "data:image/png;base64,";

  @Test
  void givenValidContentWhenGenerateCode128AsBase64ThenReturnBase64String() {
    String content = "123456789012345678";

    String result = BarcodeUtils.generateCode128AsBase64(content);

    assertNotNull(result);
    assertTrue(result.startsWith(BASE64_PNG_PREFIX));
    assertTrue(result.length() > BASE64_PNG_PREFIX.length());
  }

  @Test
  void givenValidContentAndCustomHeightWhenGenerateCode128AsBase64ThenReturnBase64String() {
    String content = "NAV123456789";
    Float customHeight = 60f;

    String result = BarcodeUtils.generateCode128AsBase64(content, customHeight);

    assertNotNull(result);
    assertTrue(result.startsWith(BASE64_PNG_PREFIX));
    assertTrue(result.length() > BASE64_PNG_PREFIX.length());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void givenInvalidContentWhenGenerateCode128AsBase64ThenReturnEmptyString(String content) {
    String result = BarcodeUtils.generateCode128AsBase64(content);

    assertEquals("", result);
  }

  @Test
  void givenNullHeightWhenGenerateCode128AsBase64ThenUseDefaultHeight() {
    String content = "TEST123";

    String result = BarcodeUtils.generateCode128AsBase64(content, null);

    assertNotNull(result);
    assertTrue(result.startsWith(BASE64_PNG_PREFIX));
  }

  @Test
  void givenExceptionWhenGenerateCode128AsBase64ThenReturnEmptyString() {
    String content = "TEST123";

    try (MockedConstruction<Barcode128> mockedBarcode = mockConstruction(Barcode128.class,
      (mock, context) -> {
        when(mock.createAwtImage(any(), any())).thenThrow(new RuntimeException("Test exception"));
      })) {

      String result = BarcodeUtils.generateCode128AsBase64(content);

      assertEquals("", result);
    }
  }
}
