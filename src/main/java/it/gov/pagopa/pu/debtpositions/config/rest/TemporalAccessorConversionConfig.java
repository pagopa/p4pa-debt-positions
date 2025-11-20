package it.gov.pagopa.pu.debtpositions.config.rest;


import it.gov.pagopa.pu.debtpositions.config.json.LocalDateTimeToOffsetDateTimeDeserializer;
import it.gov.pagopa.pu.debtpositions.config.json.OffsetDateTimeToLocalDateTimeDeserializer;
import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Configuration
public class TemporalAccessorConversionConfig {

  @Bean
  public Converter<String, LocalDateTime> string2LocalDateTimeConverter(FormatterRegistry defaultConversionService) {
    @SuppressWarnings({"Convert2Lambda", "squid:S1604"}) // Spring is not able to determine generic types otherwise
    Converter<String, LocalDateTime> converter = new Converter<>() {
      @Override
      public LocalDateTime convert(@Nonnull String source) {
        return OffsetDateTimeToLocalDateTimeDeserializer.string2LocalDateTime(source);
      }
    };
    defaultConversionService.addConverter(converter);
    return converter;
  }

  @Bean
  public Converter<String, OffsetDateTime> string2OffsetDateTimeConverter(FormatterRegistry defaultConversionService) {
    @SuppressWarnings({"Convert2Lambda", "squid:S1604"}) // Spring is not able to determine generic types otherwise
    Converter<String, OffsetDateTime> converter = new Converter<>() {
      @Override
      public OffsetDateTime convert(@Nonnull String source) {
        return LocalDateTimeToOffsetDateTimeDeserializer.string2OffsetDateTime(source);
      }
    };
    defaultConversionService.addConverter(converter);
    return converter;
  }
}
