package it.gov.pagopa.pu.debtpositions.citizen.service;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import it.gov.pagopa.pu.debtpositions.citizen.repository.PersonalDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PersonalDataService {

    private final PersonalDataRepository repository;
    private final DataCipherService dataCipherService;

    public PersonalDataService(PersonalDataRepository repository, DataCipherService dataCipherService) {
        this.repository = repository;
        this.dataCipherService = dataCipherService;
    }

    public long insert(Object pii, PersonalDataType type) {
        return repository.save(PersonalData.builder()
                .type(type.name())
                .data(dataCipherService.encryptObj(pii))
                .build()).getId();
    }

}
