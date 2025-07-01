package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TaxonomySearchClient {

  private final OrganizationApisHolder organizationApisHolder;

  public TaxonomySearchClient(OrganizationApisHolder organizationApisHolder) {
    this.organizationApisHolder = organizationApisHolder;
  }

  public PagedModelTaxonomy findTaxonomies(String organizationType, String macroAreaCode,
                                           String serviceTypeCode, String collectionReason,
                                           Integer page, Integer size, List<String> sort, String accessToken) {
    return organizationApisHolder.getTaxonomySearchControllerApi(accessToken)
      .crudTaxonomiesFindTaxonomies(organizationType, macroAreaCode, serviceTypeCode, collectionReason, page, size, sort);
  }

}
