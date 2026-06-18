package it.gov.pagopa.pu.debtpositions.config;

import it.gov.pagopa.pu.debtpositions.enums.DebtPositionTypeOrgBalanceCostType;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrgBalanceCost;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;

@ExtendWith(MockitoExtension.class)
class RepositoryRestCustomConvertersTest {

  @Mock
  private DefaultFormattingConversionService conversionServiceMock;

  private Converter<String, DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId> debtPositionTypeOrgBalanceCostIdConverter;

  @BeforeEach
  void init() {
    RepositoryRestCustomConverters repositoryRestCustomConverters = new RepositoryRestCustomConverters(conversionServiceMock);

    debtPositionTypeOrgBalanceCostIdConverter = repositoryRestCustomConverters.debtPositionTypeOrgBalanceCostIdConverter();
    Mockito.verify(conversionServiceMock).addConverter(debtPositionTypeOrgBalanceCostIdConverter);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(conversionServiceMock);
  }

//region debtPositionTypeOrgBalanceCostIdConverter test
  @Test
  void givenInvalidIdWhenDebtPositionTypeOrgBalanceCostIdConverterThenInvalidValueException() {
    InvalidValueException resultException = Assertions.assertThrows(InvalidValueException.class, () -> debtPositionTypeOrgBalanceCostIdConverter.convert("INVALID"));

    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_BALANCE_COST_INVALID_ID, resultException.getCode());
  }

  @Test
  void givenExceptionWhileParsingIdWhenDebtPositionTypeOrgBalanceCostIdConverterThenInvalidValueException() {
    InvalidValueException resultException = Assertions.assertThrows(InvalidValueException.class, () -> debtPositionTypeOrgBalanceCostIdConverter.convert("0-UNKNOWNTYPE-26"));

    Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_DEBT_POSITION_TYPE_ORG_BALANCE_COST_INVALID_ID, resultException.getCode());
  }

  @Test
  void whenDebtPositionTypeOrgBalanceCostIdConverterThenOk() {
    String idString = "0-NOTIFICATION_COST-26";
    DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId result = debtPositionTypeOrgBalanceCostIdConverter.convert(idString);

    Assertions.assertEquals(
      new DebtPositionTypeOrgBalanceCost.DebtPositionTypeOrgBalanceCostId(0L, DebtPositionTypeOrgBalanceCostType.NOTIFICATION_COST, "26"),
      result
    );

    Assertions.assertEquals(idString, result.toString());
  }
//endregion
}
