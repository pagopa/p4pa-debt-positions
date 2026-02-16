package it.gov.pagopa.pu.debtpositions.mapper.pii;

import it.gov.pagopa.pu.debtpositions.citizen.service.DataCipherService;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.mapper.TransferMapper;
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
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.checkNotNullFields;
import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InstallmentPIIMapperTest {

  @Mock
  private DataCipherService dataCipherServiceMock;
  @Mock
  private PersonalDataService personalDataServiceMock;

  private InstallmentPIIMapper mapper;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    mapper = new InstallmentPIIMapper(dataCipherServiceMock, personalDataServiceMock, new TransferMapper());
  }

  @AfterEach
  void verifyNotMoreInvocation() {
    Mockito.verifyNoMoreInteractions(
      dataCipherServiceMock,
      personalDataServiceMock);
  }

  //region map(it.gov.pagopa.pu.debtpositions.dto.Installment)

  @Test
  void testMap() {
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();

    InstallmentDTO installment = buildInstallmentDTO();
    installment.setNoPII(installmentNoPIIExpected);
    byte[] expectedHashedCF = {};
    Mockito.when(dataCipherServiceMock.hash(installment.getDebtor().getFiscalCode())).thenReturn(expectedHashedCF);

    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installment);

    reflectionEqualsByName(installmentNoPIIExpected, result.getFirst());
    reflectionEqualsByName(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "personalDataId", "debtorFiscalCodeHash");
    checkNotNullFields(result.getSecond());
  }

  @Test
  void testMapWithNoPIINotNull() {
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();

    InstallmentDTO installment = buildInstallmentDTO();
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
    installmentNoPII.getSyncStatus().setSyncError("DUMMY");
    InstallmentPIIDTO installmentPIIDTO = buildInstallmentPIIDTO();
    Mockito.when(personalDataServiceMock.get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class)).thenReturn(installmentPIIDTO);

    //when
    InstallmentDTO result = mapper.map(installmentNoPII);
    //then
    TestUtils.checkNotNullFields(result);
    TestUtils.checkNotNullFields(result.getSyncStatus());
    assertEquals(installmentPIIDTO.getOriginalRemittanceInformation(),result.getOriginalRemittanceInformation());
    Mockito.verify(personalDataServiceMock, Mockito.times(1)).get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class);
  }
  //endregion

  @Test
  void testMapAll() {
    //given
    InstallmentNoPII noPii1 = podamFactory.manufacturePojo(InstallmentNoPII.class);
    InstallmentNoPII noPii2 = podamFactory.manufacturePojo(InstallmentNoPII.class);
    List<InstallmentNoPII> noPiiDtos = List.of(noPii1, noPii2);

    InstallmentPIIDTO piiDto1 = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    InstallmentPIIDTO piiDto2 = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    Mockito.when(personalDataServiceMock.getAll(Set.of(noPii1.getPersonalDataId(), noPii2.getPersonalDataId()), InstallmentPIIDTO.class))
      .thenReturn(Map.of(
        noPii1.getPersonalDataId(), piiDto1,
        noPii2.getPersonalDataId(), piiDto2
      ));

    mapper = Mockito.spy(mapper);

    InstallmentDTO expectedFullDto1 = podamFactory.manufacturePojo(InstallmentDTO.class);
    Mockito.doReturn(expectedFullDto1)
      .when(mapper)
      .map(noPii1, piiDto1);

    InstallmentDTO expectedFullDto2 = podamFactory.manufacturePojo(InstallmentDTO.class);
    Mockito.doReturn(expectedFullDto2)
      .when(mapper)
      .map(noPii2, piiDto2);

    //when
    List<InstallmentDTO> result = mapper.mapAll(noPiiDtos);
    //then
    assertEquals(
      List.of(expectedFullDto1, expectedFullDto2),
      result
    );
  }

  @Test
  void testMapInstallmentWithNullSyncStatus() {
    InstallmentNoPII installmentNoPIIExpected = buildInstallmentNoPII();
    InstallmentPIIDTO installmentPIIDTOExpected = buildInstallmentPIIDTO();
    installmentNoPIIExpected.setSyncStatus(null);

    InstallmentDTO installment = buildInstallmentDTO();
    installment.setNoPII(installmentNoPIIExpected);
    installment.setSyncStatus(null);
    byte[] expectedHashedCF = {};
    Mockito.when(dataCipherServiceMock.hash(installment.getDebtor().getFiscalCode())).thenReturn(expectedHashedCF);

    Pair<InstallmentNoPII, InstallmentPIIDTO> result = mapper.map(installment);

    reflectionEqualsByName(installmentNoPIIExpected, result.getFirst());
    reflectionEqualsByName(installmentPIIDTOExpected, result.getSecond());
    checkNotNullFields(result.getFirst(), "personalDataId",
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
    InstallmentDTO result = mapper.map(installmentNoPII);
    //then
    TestUtils.checkNotNullFields(result, "syncStatus");
    Mockito.verify(personalDataServiceMock, Mockito.times(1)).get(installmentNoPII.getPersonalDataId(), InstallmentPIIDTO.class);
  }
}
