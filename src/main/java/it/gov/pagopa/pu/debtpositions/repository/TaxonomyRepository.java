package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.model.Taxonomy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "taxonomies")
public interface TaxonomyRepository extends JpaRepository<Taxonomy, Long> {

  boolean existsTaxonomyByTaxonomyCode(String taxonomyCode);
}
