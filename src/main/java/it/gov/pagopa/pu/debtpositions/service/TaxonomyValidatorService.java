package it.gov.pagopa.pu.debtpositions.service;

public interface TaxonomyValidatorService {
  boolean isTaxonomyCodeValid(String taxonomyCode);
  boolean isTaxonomyCategoryValid(String taxonomyCategory);
}
