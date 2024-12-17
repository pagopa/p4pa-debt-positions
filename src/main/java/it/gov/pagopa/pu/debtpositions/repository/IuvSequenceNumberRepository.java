package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.IuvSequenceNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "iuv_sequence_number", path = "iuvSequenceNumber")
public interface IuvSequenceNumberRepository extends JpaRepository<IuvSequenceNumber,Long> {
}
