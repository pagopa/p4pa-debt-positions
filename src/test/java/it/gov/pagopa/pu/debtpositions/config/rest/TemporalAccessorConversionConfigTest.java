package it.gov.pagopa.pu.debtpositions.config.rest;

import it.gov.pagopa.pu.debtpositions.config.json.LocalDateTimeToOffsetDateTimeDeserializer;
import it.gov.pagopa.pu.debtpositions.config.json.OffsetDateTimeToLocalDateTimeDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class TemporalAccessorConversionConfigTest {

  @Mock
  private FormatterRegistry formatterRegistryMock;

  private final TemporalAccessorConversionConfig config = new TemporalAccessorConversionConfig();

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(formatterRegistryMock);
  }

  @Test
  void whenString2LocalDateTimeConverterThenConfigureConverter(){
    try(MockedStatic<OffsetDateTimeToLocalDateTimeDeserializer> offsetDateTimeToLocalDateTimeDeserializerMockedStatic = Mockito.mockStatic(OffsetDateTimeToLocalDateTimeDeserializer.class)){
      // Given
      String date = "DATE";
      LocalDateTime expectedResult = LocalDateTime.now();

      offsetDateTimeToLocalDateTimeDeserializerMockedStatic.when(() -> OffsetDateTimeToLocalDateTimeDeserializer.string2LocalDateTime(date))
        .thenReturn(expectedResult);

      // When
      Converter<String, LocalDateTime> converter = config.string2LocalDateTimeConverter(formatterRegistryMock);

      LocalDateTime result = converter.convert(date);

      // Then
      Assertions.assertSame(expectedResult, result);

      Mockito.verify(formatterRegistryMock)
        .addConverter(Mockito.same(converter));
    }
  }

  @Test
  void whenString2OffsetDateTimeConverterThenConfigureConverter(){
    try(MockedStatic<LocalDateTimeToOffsetDateTimeDeserializer> localDateTimeToOffsetDateTimeDeserializerMockedStatic = Mockito.mockStatic(LocalDateTimeToOffsetDateTimeDeserializer.class)){
      // Given
      String date = "DATE";
      OffsetDateTime expectedResult = OffsetDateTime.now();

      localDateTimeToOffsetDateTimeDeserializerMockedStatic.when(() -> LocalDateTimeToOffsetDateTimeDeserializer.string2OffsetDateTime(date))
        .thenReturn(expectedResult);

      // When
      Converter<String, OffsetDateTime> converter = config.string2OffsetDateTimeConverter(formatterRegistryMock);

      OffsetDateTime result = converter.convert(date);

      // Then
      Assertions.assertSame(expectedResult, result);

      Mockito.verify(formatterRegistryMock)
        .addConverter(Mockito.same(converter));
    }
  }
}
