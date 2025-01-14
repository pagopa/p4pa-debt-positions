package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.junit.jupiter.api.Test;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static org.junit.jupiter.api.Assertions.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;

class InstallmentMapperTest {

  private final PersonMapper personMapper = new PersonMapper();
  private final TransferMapper transferMapper = new TransferMapper();
  private final InstallmentMapper installmentMapper = new InstallmentMapper(personMapper, transferMapper);

  @Test
  void givenValidInstallmentDTO_WhenMapToModel_ThenReturnInstallment() {
    Installment installmentExpected = buildInstallmentNoUpdate();
    InstallmentDTO installmentDTO = buildInstallmentDTO();

    Installment result = installmentMapper.mapToModel(installmentDTO);

    assertEquals(installmentExpected, result);
    checkNotNullFields(result, "updateOperatorExternalId", "noPII");
  }
}

