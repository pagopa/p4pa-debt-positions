package it.gov.pagopa.pu.debtpositions.connector.taxonomy;

import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import java.util.Optional;

public interface TaxonomyService {

  Optional<Taxonomy> getTaxonomyByTaxonomyCode(String taxonomyCode, String accessToken);
}
