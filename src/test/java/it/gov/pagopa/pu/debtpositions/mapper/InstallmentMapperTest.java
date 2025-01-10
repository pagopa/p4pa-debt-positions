package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;

class InstallmentMapperTest {

  private final PersonMapper personMapper = new PersonMapper();
  private final InstallmentMapper installmentMapper = new InstallmentMapper(personMapper);

  @Test
  void givenValidInstallmentDTO_WhenMapToModel_ThenReturnInstallment() {
    Installment installmentExpected = buildInstallmentNoUpdate();
    InstallmentDTO installmentDTO = buildInstallmentDTO();

    Installment result = installmentMapper.mapToModel(installmentDTO);

    assertEquals(installmentExpected, result);
  }
}

