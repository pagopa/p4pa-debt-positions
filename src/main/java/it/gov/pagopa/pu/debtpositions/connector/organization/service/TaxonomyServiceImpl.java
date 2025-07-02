package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.TaxonomySearchClient;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = it.gov.pagopa.pu.debtpositions.config.CacheConfig.Fields.taxonomy)
public class TaxonomyServiceImpl implements TaxonomyService {

  private final TaxonomySearchClient taxonomySearchClient;

  public TaxonomyServiceImpl(TaxonomySearchClient taxonomySearchClient) {
    this.taxonomySearchClient = taxonomySearchClient;
  }

  @Override
  @Cacheable(key = "#organizationType + '-' + #macroAreaCode + '-' + #serviceTypeCode + '-' + #collectionReason",
    unless = "#result.getEmbedded().getTaxonomies().size() == 0")
  public PagedModelTaxonomy getTaxonomies(String organizationType, String macroAreaCode,
                                          String serviceTypeCode, String collectionReason,
                                          Integer page, Integer size, List<String> sort, String accessToken) {
    return taxonomySearchClient.findTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, page, size, sort, accessToken);
  }

}
