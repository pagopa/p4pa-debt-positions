package it.gov.pagopa.pu.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;

@Configuration
public class RepositoryRestCustomConverters {

  private final DefaultFormattingConversionService conversionService;

  public RepositoryRestCustomConverters(DefaultFormattingConversionService conversionService) {
    this.conversionService = conversionService;
  }

  // This should be aligned with it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId#toString
  @Bean
  public Converter<String, DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId> debtPositionTypeOrgBalanceCostIdConverter() {
    @SuppressWarnings({"Convert2Lambda", "squid:S1604"}) // Suppressing lambda conversion warning, Spring is not able to retrieve the generic types otherwise
    Converter<String, DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId> converter = new Converter<>() {
      @Override
      public DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId convert(String idString) {
        String[] idTokens = idString.split("-");
        if (idTokens.length != 3) {
          throw buildInvalidDPTypeOrgBalanceCostIdException();
        }
        try {
          Long debtPositionTypeOrgId = Long.parseLong(idTokens[0]);
          DebtPositionTypeOrgBalanceCostType type = DebtPositionTypeOrgBalanceCostType.valueOf(idTokens[1]);
          String operatingYear = idTokens[2];
          return new DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId(
            debtPositionTypeOrgId,
            type,
            operatingYear
          );
        } catch (Exception e) {
          throw buildInvalidDPTypeOrgBalanceCostIdException();
        }
      }
    };

    conversionService.addConverter(converter);

    return converter;
  }
  private static @NonNull InvalidValueException buildInvalidDPTypeOrgBalanceCostIdException() {
    return new InvalidValueException(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_BALANCE_COST_INVALID_ID, "Invalid id format for DebtPositionTypeOrgBalanceCost id, expected format: {debtPositionTypeOrgId}-{type}-{operatingYear}");
  }
}
