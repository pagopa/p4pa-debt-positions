package it.gov.pagopa.pu.debtpositions.util;

import com.itextpdf.barcodes.Barcode128;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarcodeUtilsTest {

  private static final String BASE64_SVG_PREFIX = "data:image/svg+xml;base64,";

  @Test
  void givenValidContentWhenGenerateCode128AsBase64ThenReturnBase64String() {
    String content = "123456789012345678";

    String result = BarcodeUtils.generateCode128AsBase64(content);

    assertNotNull(result);
    assertTrue(result.startsWith(BASE64_SVG_PREFIX));
    assertTrue(result.length() > BASE64_SVG_PREFIX.length());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void givenInvalidContentWhenGenerateCode128AsBase64ThenReturnEmptyString(String content) {
    String result = BarcodeUtils.generateCode128AsBase64(content);

    assertEquals("", result);
  }

  @Test
  void givenExceptionWhenGenerateCode128AsBase64ThenReturnEmptyString() {
    String content = "TEST123";

    try (MockedStatic<Barcode128> mockedBarcode = mockStatic(Barcode128.class)) {
      mockedBarcode.when(() -> Barcode128.getRawText(anyString(), anyBoolean()))
        .thenThrow(new RuntimeException("Test exception"));

      String result = BarcodeUtils.generateCode128AsBase64(content);

      assertEquals("", result);
    }
  }
}
