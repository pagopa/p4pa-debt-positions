package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.IuvSequenceNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface IuvSequenceNumberRepository extends JpaRepository<IuvSequenceNumber,Long> {

  Optional<IuvSequenceNumber> findIuvSequenceNumberByOrganizationId(Long organizationId);
}
