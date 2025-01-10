package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;

class InstallmentMapperTest {

  private final TransferMapper transferMapper = new TransferMapper();
  private final InstallmentMapper installmentMapper = new InstallmentMapper(transferMapper);

  @Test
  void givenValidInstallmentDTO_WhenMapToModel_ThenReturnInstallment() {
    Installment installmentExpected = buildInstallment();
    InstallmentDTO installmentDTO = buildInstallmentNoPIIDTO();

    Installment result = installmentMapper.mapToModel(installmentDTO);

    assertEquals(installmentExpected, result);
  }
}

