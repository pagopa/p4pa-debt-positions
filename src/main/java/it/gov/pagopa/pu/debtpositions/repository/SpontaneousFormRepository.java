package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "spontaneous-forms")
public interface SpontaneousFormRepository extends JpaRepository<SpontaneousForm, Long> {
  Optional<SpontaneousForm> findByOrganizationIdAndCode(Long organizationId, String code);
  List<SpontaneousForm> findAllByOrganizationId(Long organizationId);
}
