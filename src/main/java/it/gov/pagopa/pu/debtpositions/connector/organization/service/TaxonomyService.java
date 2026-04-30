package it.gov.pagopa.pu.debtpositions.connector.organization.service;

import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;

import java.util.List;

public interface TaxonomyService {

  PagedModelTaxonomy getTaxonomies(String organizationType, String macroAreaCode,
                                   String serviceTypeCode, String collectionReason,
                                   Integer page, Integer size, List<String> sort, String accessToken);
}
