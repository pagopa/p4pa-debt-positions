package it.gov.pagopa.pu.common.pii.citizen.repository;

import it.gov.pagopa.pu.common.pii.citizen.model.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {

}
