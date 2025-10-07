package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "spontaneous-forms")
public interface SpontaneousFormRepository extends JpaRepository<SpontaneousForm, Long> {
  Optional<SpontaneousForm> findByOrganizationIdAndCode(Long organizationId, String code);
  List<SpontaneousForm> findAllByOrganizationId(Long organizationId);
  @Query("""
      SELECT s
      FROM SpontaneousForm s
      WHERE
       s.organizationId = :organizationId
       AND (:code IS NULL OR s.code ILIKE CONCAT('%', cast(:code as text), '%'))
      """)
  Page<SpontaneousForm> findAllByOrganizationIdAndCode(Long organizationId, String code, Pageable pageable);
}
