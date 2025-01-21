package it.gov.pagopa.pu.debtpositions.connector.taxonomy;

import it.gov.pagopa.pu.debtpositions.connector.taxonomy.client.TaxonomySearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaxonomyServiceImpl implements TaxonomyService {

  private final TaxonomySearchClient taxonomySearchClient;

  public TaxonomyServiceImpl(TaxonomySearchClient taxonomySearchClient) {
    this.taxonomySearchClient = taxonomySearchClient;
  }

  @Override
  public Optional<Taxonomy> getTaxonomyByTaxonomyCode(String taxonomyCode, String accessToken) {
    return Optional.ofNullable(
      taxonomySearchClient.findByTaxonomyCode(taxonomyCode, accessToken)
    );
  }
}
