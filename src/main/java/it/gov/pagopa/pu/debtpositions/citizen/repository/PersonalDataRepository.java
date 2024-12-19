package it.gov.pagopa.pu.debtpositions.citizen.repository;

import it.gov.pagopa.pu.debtpositions.citizen.model.PersonalData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalDataRepository extends JpaRepository<PersonalData, Long> {

}
