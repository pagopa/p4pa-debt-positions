package it.gov.pagopa.pu.debtpositions.citizen.service;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import it.gov.pagopa.pu.debtpositions.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.config.CacheConfig;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PersonalDataServiceTest {

  @Mock
  private PersonalDataRepository repositoryMock;
  @Mock
  private DataCipherService cipherServiceMock;
  @Mock
  private CacheManager cacheManagerMock;

  private PersonalDataService service;

  private ConcurrentMapCache cache;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void init() {
    cache = new ConcurrentMapCache(CacheConfig.Fields.pii);
    Mockito.when(cacheManagerMock.getCache(CacheConfig.Fields.pii)).thenReturn(cache);

    service = new PersonalDataService(
      repositoryMock,
      cipherServiceMock,
      cacheManagerMock
    );
  }

  @AfterEach
  void verifyNotMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      repositoryMock,
      cipherServiceMock,
      cacheManagerMock
    );
  }

  @Test
  void testInsert() {
    // Given
    InstallmentPIIDTO pii = new InstallmentPIIDTO();

    byte[] cipherData = new byte[0];
    Mockito.when(cipherServiceMock.encryptObj(pii)).thenReturn(cipherData);
    PersonalData personalDataInput = PersonalData.builder()
      .type("INSTALLMENT")
      .data(cipherData)
      .build();

    long piiId = -1L;
    PersonalData personalDataOutput = PersonalData.builder()
      .id(piiId)
      .type("INSTALLMENT")
      .data(cipherData)
      .build();

    Mockito.when(repositoryMock.save(personalDataInput)).thenReturn(personalDataOutput);

    // When
    long insert = service.insert(pii, PersonalDataType.INSTALLMENT);

    // Then
    Assertions.assertEquals(piiId, insert);
    Assertions.assertSame(pii, cache.get(piiId, pii.getClass()));
  }

  //region get
  @Test
  void givenValidPersonalDataIdWhenGetThenOk() {
    // Given
    long personalDataId = 1L;
    InstallmentPIIDTO expected = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    Mockito.when(repositoryMock.findById(personalDataId)).thenReturn(
      Optional.of(PersonalData.builder().id(personalDataId).data(new byte[0]).type(PersonalDataType.INSTALLMENT.name()).build()));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], InstallmentPIIDTO.class)).thenReturn(expected);

    // When
    InstallmentPIIDTO installmentPIIDTO = service.get(personalDataId, InstallmentPIIDTO.class);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, installmentPIIDTO, true, null, true));
  }

  @Test
  void givenNotFoundPersonalDataIdWhenGetThenException() {
    // Given
    long personalDataId = 1L;
    Mockito.when(repositoryMock.findById(personalDataId)).thenReturn(Optional.empty());

    // When
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.get(personalDataId, InstallmentPIIDTO.class));

    // Then
    Assertions.assertEquals("[PII_ENTITY_NOT_FOUND] PII Entity with id 1 not found", notFoundException.getMessage());
  }
//endregion

  //region getAll
  @Test
  void givenValidPersonalDataIdsWhenGetAllThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> personalDataIds = Set.of(pId1, pId2);
    InstallmentPIIDTO pii1 = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    InstallmentPIIDTO pii2 = podamFactory.manufacturePojo(InstallmentPIIDTO.class);
    cache.put(pId2, pii2);

    Mockito.when(repositoryMock.findAllById(personalDataIds)).thenReturn(List.of(
      PersonalData.builder().id(pId1).data(new byte[0]).type(PersonalDataType.INSTALLMENT.name()).build(),
      PersonalData.builder().id(pId2).data(new byte[0]).type(PersonalDataType.INSTALLMENT.name()).build()
    ));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], InstallmentPIIDTO.class)).thenReturn(pii1);

    // When
    Map<Long, InstallmentPIIDTO> results = service.getAll(personalDataIds, InstallmentPIIDTO.class);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
  }

  @Test
  void givenNotFoundPersonalDataIdsWhenGetAllThenException() {
    // Given
    Set<Long> personalDataIds = Set.of(1L, 2L);
    Mockito.when(repositoryMock.findAllById(personalDataIds)).thenReturn(List.of(
      PersonalData.builder().id(1L).data(new byte[0]).type(PersonalDataType.INSTALLMENT.name()).build()));
    Mockito.when(cipherServiceMock.decryptObj(new byte[0], InstallmentPIIDTO.class)).thenReturn(podamFactory.manufacturePojo(InstallmentPIIDTO.class));

    // When
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.getAll(personalDataIds, InstallmentPIIDTO.class));

    // Then
    Assertions.assertEquals("[PII_ENTITY_NOT_FOUND] PII Entities with ids 2 not found", notFoundException.getMessage());
  }
//endregion

  @Test
  void whenGet2AllThenOk() {
    // Given
    Set<Long> pDataIds1 = LongStream.range(0, 10).boxed().collect(Collectors.toSet());
    Class<InstallmentPIIDTO> classType1 = InstallmentPIIDTO.class;
    Map<Long, InstallmentPIIDTO> expectedPData1Dtos = Map.of();

    Set<Long> pDataIds2 = LongStream.range(pDataIds1.size(), 10).boxed().collect(Collectors.toSet());
    Class<ReceiptPIIDTO> classType2 = ReceiptPIIDTO.class;
    Map<Long, ReceiptPIIDTO> expectedPData2Dtos = Map.of();

    service = Mockito.spy(service);

    List<PersonalData> pData = List.of();

    Mockito.when(repositoryMock.findAllById(Stream.concat(
        pDataIds1.stream(),
        pDataIds2.stream()
      ).toList()))
      .thenReturn(pData);

    Mockito.doReturn(expectedPData1Dtos)
      .when(service)
      .getAll(Mockito.same(pData), Mockito.same(pDataIds1), Mockito.same(classType1));

    Mockito.doReturn(expectedPData2Dtos)
      .when(service)
      .getAll(Mockito.same(pData), Mockito.same(pDataIds2), Mockito.same(classType2));

    // When
    Pair<Map<Long, InstallmentPIIDTO>, Map<Long, ReceiptPIIDTO>> results = service.get2All(
      pDataIds1, classType1,
      pDataIds2, classType2
    );

    //Then
    Assertions.assertSame(results.getLeft(), expectedPData1Dtos);
    Assertions.assertSame(results.getRight(), expectedPData2Dtos);
  }

  @Test
  void testDelete() {
    // Given
    long id = 1L;

    // When
    service.delete(id);

    // Then
    Mockito.verify(repositoryMock).deleteById(id);
  }
}
