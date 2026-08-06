package it.gov.pagopa.pu.common.pii.citizen.service;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import it.gov.pagopa.pu.common.pii.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.config.CacheConfig;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PersonalDataServiceTest {

  public static final Class<InstallmentPIIDTO> CLASS_PII_DTO = InstallmentPIIDTO.class;
  public static final PersonalDataType PERSONAL_DATA_TYPE = PersonalDataType.INSTALLMENT;
  public static final Class<ReceiptPIIDTO> CLASS_PII_DTO2 = ReceiptPIIDTO.class;

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
    when(cacheManagerMock.getCache(CacheConfig.Fields.pii)).thenReturn(cache);

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
    when(cipherServiceMock.encryptObj(pii)).thenReturn(cipherData);
    PersonalData personalDataInput = PersonalData.builder()
      .type(PERSONAL_DATA_TYPE.name())
      .data(cipherData)
      .build();

    long piiId = -1L;
    PersonalData personalDataOutput = PersonalData.builder()
      .id(piiId)
      .type(PERSONAL_DATA_TYPE.name())
      .data(cipherData)
      .build();

    when(repositoryMock.save(personalDataInput)).thenReturn(personalDataOutput);

    // When
    long insert = service.insert(pii, PERSONAL_DATA_TYPE);

    // Then
    Assertions.assertEquals(piiId, insert);
    Assertions.assertSame(pii, cache.get(piiId, pii.getClass()));
  }

  //region get
  @Test
  void givenValidPersonalDataIdWhenGetThenOk() {
    // Given
    long personalDataId = 1L;
    InstallmentPIIDTO expected = podamFactory.manufacturePojo(CLASS_PII_DTO);
    when(repositoryMock.findById(personalDataId)).thenReturn(
      Optional.of(PersonalData.builder().id(personalDataId).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()));
    when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(expected);

    // When
    InstallmentPIIDTO result = service.get(personalDataId, CLASS_PII_DTO);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(expected, result, true, null, true));
  }

  @Test
  void givenNotFoundPersonalDataIdWhenGetThenException() {
    // Given
    long personalDataId = 1L;
    when(repositoryMock.findById(personalDataId)).thenReturn(Optional.empty());

    // When
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.get(personalDataId, CLASS_PII_DTO));

    // Then
    Assertions.assertEquals("PII Entity with id 1 not found", notFoundException.getMessage());
    Assertions.assertEquals("PII_ENTITY_NOT_FOUND", notFoundException.getCode());
  }
//endregion

  //region getAll
  @Test
  void givenPartialCachedPDataIdsWhenGetAllThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> personalDataIds = Set.of(pId1, pId2);
    InstallmentPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    InstallmentPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);

    Set<Long> cacheMissedPIds = Set.of(pId1);
    cache.put(pId2, pii2);

    when(repositoryMock.findAllById(cacheMissedPIds)).thenReturn(List.of(
      PersonalData.builder().id(pId1).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()
    ));
    when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(pii1);

    // When
    Map<Long, InstallmentPIIDTO> results = service.getAll(personalDataIds, CLASS_PII_DTO);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
    Assertions.assertSame(pii2, results.get(pId2));
  }

  @Test
  void givenCompleteCachedPDataIdsWhenGetAllThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> personalDataIds = Set.of(pId1, pId2);
    InstallmentPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    InstallmentPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);

    cache.put(pId1, pii1);
    cache.put(pId2, pii2);

    // When
    Map<Long, InstallmentPIIDTO> results = service.getAll(personalDataIds, CLASS_PII_DTO);

    //Then
    Assertions.assertSame(pii1, results.get(pId1));
    Assertions.assertSame(pii2, results.get(pId2));
  }

  @Test
  void givenCachedPiiWhenGetAllProtectedThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> pDataIds = Set.of(pId1, pId2);
    InstallmentPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    InstallmentPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    cache.put(pId2, pii2);

    List<PersonalData> pData = List.of(
      PersonalData.builder().id(pId1).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build(),
      PersonalData.builder().id(pId2).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()
    );

    when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(pii1);

    // When
    Map<Long, InstallmentPIIDTO> results = service.getAll(pData, pDataIds, CLASS_PII_DTO);

    //Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
    Assertions.assertSame(pii2, results.get(pId2));
  }

  @Test
  void givenNotFoundPersonalDataIdsWhenGetAllThenException() {
    // Given
    Set<Long> personalDataIds = Set.of(1L, 2L);
    when(repositoryMock.findAllById(personalDataIds)).thenReturn(List.of(
      PersonalData.builder().id(1L).data(new byte[0]).type(PERSONAL_DATA_TYPE.name()).build()));
    when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO)).thenReturn(podamFactory.manufacturePojo(CLASS_PII_DTO));

    // When
    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> service.getAll(personalDataIds, CLASS_PII_DTO));

    // Then
    Assertions.assertEquals("PII_ENTITY_NOT_FOUND", notFoundException.getCode());
    Assertions.assertEquals("PII Entities with ids 2 not found", notFoundException.getMessage());
  }

  @Test
  void whenGetAllPrivateThenOk() {
    // Given
    long pId1 = 1L;
    long pId2 = 2L;
    Set<Long> searchedPDataIds = Set.of(pId1, pId2);
    List<PersonalData> pDatas = List.of(
      PersonalData.builder()
        .id(pId1)
        .data(new byte[0])
        .type(PERSONAL_DATA_TYPE.name())
        .build(),
      PersonalData.builder()
        .id(pId2)
        .data(new byte[0])
        .type(PERSONAL_DATA_TYPE.name())
        .build(),
      PersonalData.builder()
        .id(3L)
        .data(new byte[0])
        .type(PERSONAL_DATA_TYPE.name())
        .build()
    );

    InstallmentPIIDTO pii1 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    InstallmentPIIDTO pii2 = podamFactory.manufacturePojo(CLASS_PII_DTO);
    when(cipherServiceMock.decryptObj(new byte[0], CLASS_PII_DTO))
      .thenReturn(pii1)
      .thenReturn(pii2);

    // When
    Map<Long, InstallmentPIIDTO> results = service.getAll(pDatas, searchedPDataIds, CLASS_PII_DTO);

    // Then
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii1, results.get(pId1), true, null, true));
    Assertions.assertTrue(EqualsBuilder.reflectionEquals(pii2, results.get(pId2), true, null, true));
  }
//endregion

  @Test
  void givenPartialCacheHitWhenGet2AllThenOk() {
    // Given
    Set<Long> pDataIds1 = LongStream.range(0, 10).boxed().collect(Collectors.toSet());
    Class<InstallmentPIIDTO> classType1 = CLASS_PII_DTO;
    Map<Long, InstallmentPIIDTO> expectedPData1Retrieved = Map.of(0L, new InstallmentPIIDTO());
    long pDataId1Cached = 5;
    InstallmentPIIDTO pData1Cached = new InstallmentPIIDTO();
    cache.put(pDataId1Cached, pData1Cached);

    Set<Long> pDataIds2 = LongStream.range(pDataIds1.size(), 20).boxed().collect(Collectors.toSet());
    Class<ReceiptPIIDTO> classType2 = CLASS_PII_DTO2;
    Map<Long, ReceiptPIIDTO> expectedPData2Retrieved = Map.of(10L, new ReceiptPIIDTO());
    long pDataId2Cached = 15;
    ReceiptPIIDTO pData2Cached = new ReceiptPIIDTO();
    cache.put(pDataId2Cached, pData2Cached);

    Map<Long, InstallmentPIIDTO> expectedPData1Result = new HashMap<>();
    expectedPData1Result.put(pDataId1Cached, pData1Cached);
    expectedPData1Result.putAll(expectedPData1Retrieved);

    Map<Long, ReceiptPIIDTO> expectedPData2Result = new HashMap<>();
    expectedPData2Result.put(pDataId2Cached, pData2Cached);
    expectedPData2Result.putAll(expectedPData2Retrieved);

    service = spy(service);

    List<PersonalData> pData = List.of();

    Set<Long> expectedPii1CacheMiss = pDataIds1.stream().filter(id -> id != pDataId1Cached).collect(Collectors.toSet());
    Set<Long> expectedPii2CacheMiss = pDataIds2.stream().filter(id -> id != pDataId2Cached).collect(Collectors.toSet());

    when(repositoryMock.findAllById(Stream.concat(
        expectedPii1CacheMiss.stream(),
        expectedPii2CacheMiss.stream()
      ).toList()))
      .thenReturn(pData);

    doReturn(expectedPData1Retrieved)
      .when(service)
      .getAll(Mockito.same(pData), Mockito.eq(expectedPii1CacheMiss), Mockito.same(classType1));

    doReturn(expectedPData2Retrieved)
      .when(service)
      .getAll(Mockito.same(pData), Mockito.eq(expectedPii2CacheMiss), Mockito.same(classType2));

    // When
    Pair<Map<Long, InstallmentPIIDTO>, Map<Long, ReceiptPIIDTO>> results = service.get2All(
      pDataIds1, classType1,
      pDataIds2, classType2
    );

    //Then
    Assertions.assertEquals(expectedPData1Result, results.getLeft());
    Assertions.assertEquals(expectedPData2Result, results.getRight());
  }

  @Test
  void givenCompleteCacheHitWhenGet2AllThenDontRetrievethem() {
    // Given
    Map<Long, InstallmentPIIDTO> pData1CacheHit = new HashMap<>();
    Set<Long> pDataIds1 = LongStream.range(0, 10).boxed()
      .peek(id -> {
        InstallmentPIIDTO pii = new InstallmentPIIDTO();
        pData1CacheHit.put(id, pii);
        cache.put(id, pii);
      })
      .collect(Collectors.toSet());

    Map<Long, ReceiptPIIDTO> pData2CacheHit = new HashMap<>();
    Set<Long> pDataIds2 = LongStream.range(pDataIds1.size(), 20).boxed()
      .peek(id -> {
        ReceiptPIIDTO pii = new ReceiptPIIDTO();
        pData2CacheHit.put(id, pii);
        cache.put(id, pii);
      })
      .collect(Collectors.toSet());

    // When
    Pair<Map<Long, InstallmentPIIDTO>, Map<Long, ReceiptPIIDTO>> results = service.get2All(
      pDataIds1, CLASS_PII_DTO,
      pDataIds2, CLASS_PII_DTO2
    );

    //Then
    Assertions.assertEquals(pData1CacheHit, results.getLeft());
    Assertions.assertEquals(pData2CacheHit, results.getRight());
  }

  @Test
  void testDelete() {
    // Given
    long id = 1L;

    // When
    service.delete(id);

    // Then
    verify(repositoryMock).deleteById(id);
  }
}
