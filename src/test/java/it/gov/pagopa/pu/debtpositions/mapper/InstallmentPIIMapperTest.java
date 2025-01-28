package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentPIIMapperTest {

  private InstallmentPIIMapper mapper;

  @Mock
  private DataCipherService dataCipherServiceMock;

  @Mock
  private PersonalDataService personalDataServiceMock;

  @BeforeEach
  void init() {
    mapper = new InstallmentPIIMapper(dataCipherServiceMock, personalDataServiceMock);
  }

  @AfterEach
  void verifyNotMoreInvocation() {
    Mockito.verifyNoMoreInteractions(dataCipherServiceMock);
  }

  //region map(it.gov.pagopa.pu.debtpositions.dto.Installment)

  @Test
  void testMap() {
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();

    Installment installment = buildInstallment();
    byte[] expectedHashedCF = {};
    Mockito.when(dataCipherServiceMock.hash(installment.getDebtor().getFiscalCode())).thenReturn(expectedHashedCF);

    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installment);

    reflectionEqualsByName(installmentNoPIIExpected, result.getFirst());
    reflectionEqualsByName(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "transfers", "personalDataId", "debtorFiscalCodeHash");
    checkNotNullFields(result.getSecond());
  }

  @Test
  void testMapWithNoPIINotNull() {
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();

    Installment installment = buildInstallment();
    installment.setNoPII(installmentNoPIIExpected);
    byte[] expectedHashedCF = {};
    Mockito.when(dataCipherServiceMock.hash(installment.getDebtor().getFiscalCode())).thenReturn(expectedHashedCF);

    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installment);

    assertEquals(installmentNoPIIExpected, result.getFirst());
    assertEquals(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "transfers", "personalDataId", "debtorFiscalCodeHash");
    checkNotNullFields(result.getSecond());
  }

  //endregion

  //region map(it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII)
  @Test
  void testMapInstallmentNoPII() {
    //given
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTO = buildInstallmentPIIDTO();
    Mockito.when(personalDataServiceMock.get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);

    //when
    Installment result = mapper.map(installmentNoPII);
    //then
    TestUtils.checkNotNullFields(result);
    TestUtils.checkNotNullFields(result.getSyncStatus());
    Mockito.verify(personalDataServiceMock, Mockito.times(1)).get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class);
  }
  //endregion

  @Test
  void testMapInstallmentWithNullSyncStatus() {
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();
    installmentNoPIIExpected.setSyncStatus(null);

    Installment installment = buildInstallment();
    installment.setSyncStatus(null);
    byte[] expectedHashedCF = {};
    Mockito.when(dataCipherServiceMock.hash(installment.getDebtor().getFiscalCode())).thenReturn(expectedHashedCF);

    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installment);

    reflectionEqualsByName(installmentNoPIIExpected, result.getFirst());
    reflectionEqualsByName(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "transfers", "personalDataId",
      "debtorFiscalCodeHash", "syncStatus");
    checkNotNullFields(result.getSecond());
  }

  @Test
  void testMapInstallmentNoPIIWithNullSyncStatus() {
    //given
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    installmentNoPII.setSyncStatus(null);
    InstallmentPIIDTO installmentPIIDTO = buildInstallmentPIIDTO();
    Mockito.when(personalDataServiceMock.get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);

    //when
    Installment result = mapper.map(installmentNoPII);
    //then
    TestUtils.checkNotNullFields(result, "syncStatus");
    Mockito.verify(personalDataServiceMock, Mockito.times(1)).get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class);
  }
}
