package it.gov.pagopa.pu.common.pii.citizen.service;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import it.gov.pagopa.pu.common.pii.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@CacheConfig(cacheNames = it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.pii)
public class PersonalDataService {

  private final PersonalDataRepository repository;
  private final DataCipherService dataCipherService;
  private final Cache piiCache;

  public PersonalDataService(PersonalDataRepository repository, DataCipherService dataCipherService, CacheManager cacheManager) {
    this.repository = repository;
    this.dataCipherService = dataCipherService;
    piiCache = Objects.requireNonNull(cacheManager.getCache(it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.pii));
  }

  public long insert(Object pii, PersonalDataType type) {
    Long personalDataId = repository.save(PersonalData.builder()
      .type(type.name())
      .data(dataCipherService.encryptObj(pii))
      .build()).getId();
    piiCache.put(personalDataId, pii);
    return personalDataId;
  }

  @CacheEvict(key = "#personalDataId")
  public void delete(long personalDataId) {
    repository.deleteById(personalDataId);
  }

  @Cacheable(key = "#personalDataId", unless = "#result == null")
  public <T> T get(long personalDataId, Class<T> classType) {
    return repository.findById(personalDataId)
      .map(personalData -> dataCipherService.decryptObj(personalData.getData(), classType))
      .orElseThrow(() -> new NotFoundException("[PII_ENTITY_NOT_FOUND] PII Entity with id " + personalDataId + " not found"));
  }

  public <T> Map<Long, T> getAll(Set<Long> personalDataIds, Class<T> classType) {
    List<PersonalData> pData = repository.findAllById(personalDataIds);
    return getAll(pData, personalDataIds, classType);
  }

  public <T,P> Pair<Map<Long, T>, Map<Long, P>> get2All(
    Set<Long> personalDataIds1, Class<T> classType1,
    Set<Long> personalDataIds2, Class<P> classType2
  ) {
    List<Long> ids = Stream.concat(
      personalDataIds1.stream(),
      personalDataIds2.stream()
    ).toList();

    List<PersonalData> pData = repository.findAllById(ids);
    Map<Long, T> out1 = getAll(pData, personalDataIds1, classType1);
    Map<Long, P> out2 = getAll(pData, personalDataIds2, classType2);

    return Pair.of(out1, out2);
  }

  protected <T> Map<Long, T> getAll(List<PersonalData> pData, Set<Long> personalDataIds, Class<T> classType) {
    Map<Long, T> result = pData.stream()
      .collect(Collectors.toMap(
        PersonalData::getId,
        personalData -> {
          T cachedValue = piiCache.get(personalData.getId(), classType);
          if (cachedValue != null) {
            return cachedValue;
          } else {
            return dataCipherService.decryptObj(personalData.getData(), classType);
          }
        })
      );

    if(result.size() != personalDataIds.size()) {
      String personalDataIdsNotFound = personalDataIds.stream()
        .filter(id -> result.get(id) == null)
        .map(String::valueOf)
        .collect(Collectors.joining(","));
      throw new NotFoundException("[PII_ENTITY_NOT_FOUND] PII Entities with ids " + personalDataIdsNotFound + " not found");
    }
    return result;
  }

}
