package it.gov.pagopa.pu.debtpositions.connector.taxonomy.client;

import it.gov.pagopa.pu.debtpositions.connector.taxonomy.config.TaxonomyApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.springframework.stereotype.Service;

@Service
public class TaxonomySearchClient {

  private final TaxonomyApisHolder taxonomyApisHolder;

  public TaxonomySearchClient(TaxonomyApisHolder taxonomyApisHolder) {
    this.taxonomyApisHolder = taxonomyApisHolder;
  }

  public Taxonomy findByTaxonomyCode(String taxonomyCode, String accessToken) {
    return taxonomyApisHolder.getTaxonomyCodeDtoSearchControllerApi(accessToken)
      .crudTaxonomiesFindByTaxonomyCode(taxonomyCode);
  }
}
