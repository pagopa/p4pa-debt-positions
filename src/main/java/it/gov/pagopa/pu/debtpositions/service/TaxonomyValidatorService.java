package it.gov.pagopa.pu.debtpositions.service;

public interface TaxonomyValidatorService {
  void validateTaxonomyCategory(String taxonomyCategory);
  boolean isTaxonomyCodeValid(String taxonomyCode, String orgTypeCode);
  boolean isTaxonomyCategoryValid(String taxonomyCategory, String orgTypeCode);
}
