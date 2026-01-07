package it.gov.pagopa.pu.debtpositions.citizen.service;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import it.gov.pagopa.pu.debtpositions.citizen.repository.PersonalDataRepository;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@CacheConfig(cacheNames = it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.pii)
public class PersonalDataService {

  private final PersonalDataRepository repository;
  private final DataCipherService dataCipherService;
  private final CacheManager cacheManager;

  public PersonalDataService(PersonalDataRepository repository, DataCipherService dataCipherService, CacheManager cacheManager) {
    this.repository = repository;
    this.dataCipherService = dataCipherService;
    this.cacheManager = cacheManager;
  }

  public long insert(Object pii, PersonalDataType type) {
    Long personalDataId = repository.save(PersonalData.builder()
      .type(type.name())
      .data(dataCipherService.encryptObj(pii))
      .build()).getId();
    Objects.requireNonNull(cacheManager.getCache(it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.pii))
      .put(personalDataId, pii);
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

}
