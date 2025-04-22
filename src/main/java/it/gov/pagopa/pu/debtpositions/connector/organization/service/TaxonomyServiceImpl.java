package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.TaxonomySearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.taxonomy)
public class TaxonomyServiceImpl implements TaxonomyService {

  private final TaxonomySearchClient taxonomySearchClient;

  public TaxonomyServiceImpl(TaxonomySearchClient taxonomySearchClient) {
    this.taxonomySearchClient = taxonomySearchClient;
  }

  @Override
  @Cacheable(key = "#taxonomyCode", unless = "#result == null")
  public Optional<Taxonomy> getTaxonomyByTaxonomyCode(String taxonomyCode, String accessToken) {
    return Optional.ofNullable(
      taxonomySearchClient.findByTaxonomyCode(taxonomyCode, accessToken)
    );
  }
}
