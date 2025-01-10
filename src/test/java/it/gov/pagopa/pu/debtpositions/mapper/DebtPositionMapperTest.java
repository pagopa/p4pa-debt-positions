package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.*;
import static org.junit.jupiter.api.Assertions.*;

class DebtPositionMapperTest {

  private final PaymentOptionMapper paymentOptionMapper = new PaymentOptionMapper();
  private final DebtPositionMapper debtPositionMapper = new DebtPositionMapper(paymentOptionMapper);

  @Test
  void givenValidDebtPositionDTO_whenMapToModel_thenReturnDebtPosition() {
    DebtPosition debtPositionExpected = buildDebtPosition();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPosition result = debtPositionMapper.mapToModel(debtPositionDTO);

    assertEquals(debtPositionExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId");
  }
}
