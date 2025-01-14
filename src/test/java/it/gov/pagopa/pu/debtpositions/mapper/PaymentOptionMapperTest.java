package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.util.Pair;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.*;
import static org.junit.jupiter.api.Assertions.*;

class PaymentOptionMapperTest {

  @Mock
  private DataCipherService dataCipherServiceMock;

  private final PersonMapper personMapper = new PersonMapper();
  private final TransferMapper transferMapper = new TransferMapper();
  private final InstallmentMapper installmentMapper = new InstallmentMapper(personMapper, transferMapper);
  private final InstallmentPIIMapper installmentPIIMapper = new InstallmentPIIMapper(dataCipherServiceMock);
  private final PaymentOptionMapper paymentOptionMapper = new PaymentOptionMapper(installmentMapper, installmentPIIMapper);

  @Test
  void givenValidPaymentOptionDTO_WhenMapToModel_ThenReturnPaymentOptionAndInstallmentMap() {
    PaymentOption paymentOptionExpected = buildPaymentOption();
    PaymentOptionDTO paymentOptionDTO = buildPaymentOptionDTO();

    Pair<PaymentOption, Map<InstallmentNoPII, Installment>> result = paymentOptionMapper.mapToModel(paymentOptionDTO);

    assertEquals(paymentOptionExpected, result.getFirst());
    checkNotNullFields(result.getFirst(), "updateOperatorExternalId", "creationDate", "updateDate");
  }
}
