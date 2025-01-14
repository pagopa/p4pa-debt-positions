package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.util.Pair;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.*;
import static org.junit.jupiter.api.Assertions.*;

class DebtPositionMapperTest {

  @Mock
  private DataCipherService dataCipherServiceMock;

  private DebtPositionMapper debtPositionMapper;

  @BeforeEach
  void init(){
    PersonMapper personMapper = new PersonMapper();
    TransferMapper transferMapper = new TransferMapper();
    InstallmentMapper installmentMapper = new InstallmentMapper(personMapper, transferMapper);
    InstallmentPIIMapper installmentPIIMapper = new InstallmentPIIMapper(dataCipherServiceMock);
    PaymentOptionMapper paymentOptionMapper = new PaymentOptionMapper(installmentMapper, installmentPIIMapper);
    debtPositionMapper = new DebtPositionMapper(paymentOptionMapper);
  }

  @Test
  void givenValidDebtPositionDTO_whenMapToModel_thenReturnDebtPositionAndInstallmentMap() {
    DebtPosition debtPositionExpected = buildDebtPosition();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> result = debtPositionMapper.mapToModel(debtPositionDTO);

    assertEquals(debtPositionExpected, result.getFirst());
    checkNotNullFields(result.getFirst(), "updateOperatorExternalId");
  }

}
