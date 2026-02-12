package it.gov.pagopa.pu.debtpositions.citizen.service;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import it.gov.pagopa.pu.debtpositions.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    Map<Long, T> result = repository.findAllById(personalDataIds).stream()
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
