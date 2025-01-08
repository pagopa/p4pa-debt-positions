package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InstallmentPIIMapperTest {

  private InstallmentPIIMapper mapper;

  @BeforeEach
  void init(){
    mapper = new InstallmentPIIMapper();
  }

  @Test
  void testMap(){
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();

    InstallmentDTO installmentDTO = buildInstallmentDTO();
    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installmentDTO);

    assertEquals(installmentNoPIIExpected, result.getFirst());
    assertEquals(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "transfers", "personalDataId", "debtorFiscalCodeHash");
    checkNotNullFields(result.getSecond());
  }

}
