package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentPIIMapperTest {

  private InstallmentPIIMapper mapper;

  @Mock
  private DataCipherService dataCipherServiceMock;

  @BeforeEach
  void init(){
    mapper = new InstallmentPIIMapper(dataCipherServiceMock);
  }

  @AfterEach
  void verifyNotMoreInvocation() {
    Mockito.verifyNoMoreInteractions(dataCipherServiceMock);
  }

  @Test
  void testMap(){
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();

    InstallmentDTO installmentDTO = buildInstallmentDTO();
    byte[] expectedHashedCF = {};
    Mockito.when(dataCipherServiceMock.hash(installmentDTO.getDebtor().getUniqueIdentifierCode())).thenReturn(expectedHashedCF);

    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installmentDTO);

    assertEquals(installmentNoPIIExpected, result.getFirst());
    assertEquals(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "transfers", "personalDataId", "debtorFiscalCodeHash");
    checkNotNullFields(result.getSecond());
  }

}
