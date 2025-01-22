package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.springframework.stereotype.Service;

@Service
public class TaxonomySearchClient {

  private final OrganizationApisHolder organizationApisHolder;

  public TaxonomySearchClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public Taxonomy findByTaxonomyCode(String taxonomyCode, String accessToken) {
    return organizationApisHolder.getTaxonomyCodeDtoSearchControllerApi(accessToken)
      .crudTaxonomiesFindByTaxonomyCode(taxonomyCode);
  }
}
