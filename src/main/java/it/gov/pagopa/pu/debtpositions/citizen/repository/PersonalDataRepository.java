package it.gov.pagopa.pu.debtpositions.citizen.repository;

import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {

}
