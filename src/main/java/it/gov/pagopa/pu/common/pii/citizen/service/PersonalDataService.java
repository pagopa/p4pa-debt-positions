package it.gov.pagopa.pu.common.pii.citizen.service;

import it.gov.pagopa.pu.common.pii.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import it.gov.pagopa.pu.common.pii.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
      .orElseThrow(() -> new NotFoundException("PII_ENTITY_NOT_FOUND", "PII Entity with id " + personalDataId + " not found"));
  }

  /**
   * It will inspect the cache in order to resolve the personalDataIds provided, returning the results and the missing ids
   */
  private <T> Pair<HashMap<Long, T>, Set<Long>> getAllThroughCache(Set<Long> personalDataIds, Class<T> classType) {
    HashMap<Long, T> cacheHit = HashMap.newHashMap(personalDataIds.size());
    Set<Long> missingIds = personalDataIds.stream()
      .filter(pId -> {
        T pii = piiCache.get(pId, classType);
        if (pii != null) {
          cacheHit.put(pId, pii);
          return false;
        } else {
          return true;
        }
      })
      .collect(Collectors.toSet());
    return Pair.of(cacheHit, missingIds);
  }

  public <T> Map<Long, T> getAll(Set<Long> personalDataIds, Class<T> classType) {
    Pair<HashMap<Long, T>, Set<Long>> cacheHit2MissingIds = getAllThroughCache(personalDataIds, classType);
    HashMap<Long, T> result = cacheHit2MissingIds.getLeft();
    Set<Long> cacheMissIds = cacheHit2MissingIds.getRight();

    if(result.size() != personalDataIds.size()) {
      List<PersonalData> pData = repository.findAllById(cacheMissIds);
      result.putAll(getAll(pData, cacheMissIds, classType));
    }

    return result;
  }

  public <T, P> Pair<Map<Long, T>, Map<Long, P>> get2All(
    Set<Long> personalDataIds1, Class<T> classType1,
    Set<Long> personalDataIds2, Class<P> classType2
  ) {
    Pair<HashMap<Long, T>, Set<Long>> result1CacheHit2MissingIds = getAllThroughCache(personalDataIds1, classType1);
    Pair<HashMap<Long, P>, Set<Long>> result2CacheHit2MissingIds = getAllThroughCache(personalDataIds2, classType2);

    HashMap<Long, T> result1 = result1CacheHit2MissingIds.getLeft();
    Set<Long> cacheMissIds1 = result1CacheHit2MissingIds.getRight();
    HashMap<Long, P> result2 = result2CacheHit2MissingIds.getLeft();
    Set<Long> cacheMissIds2 = result2CacheHit2MissingIds.getRight();


    List<Long> cacheMissIds = Stream.concat(
      cacheMissIds1.stream(),
      cacheMissIds2.stream()
    ).toList();

    if(!CollectionUtils.isEmpty(cacheMissIds)) {
      List<PersonalData> pData = repository.findAllById(cacheMissIds);
      result1.putAll(getAll(pData, cacheMissIds1, classType1));
      result2.putAll(getAll(pData, cacheMissIds2, classType2));
    }

    return Pair.of(result1, result2);
  }

  protected <T> Map<Long, T> getAll(List<PersonalData> pData, Set<Long> personalDataIds, Class<T> classType) {
    Map<Long, T> result = pData.stream()
      .filter(p -> personalDataIds.contains(p.getId()))
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

    if (result.size() != personalDataIds.size()) {
      String personalDataIdsNotFound = personalDataIds.stream()
        .filter(id -> result.get(id) == null)
        .map(String::valueOf)
        .collect(Collectors.joining(","));
      throw new NotFoundException("PII_ENTITY_NOT_FOUND", "PII Entities with ids " + personalDataIdsNotFound + " not found");
    }
    return result;
  }

}
