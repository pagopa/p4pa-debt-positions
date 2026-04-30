package it.gov.pagopa.pu.debtpositions.service;

public interface TaxonomyValidatorService {
  boolean validateTaxonomyCategory(String taxonomyCategory, String orgFiscalCode);
  boolean isTaxonomyCodeValid(String taxonomyCode, String orgTypeCode);
  boolean isTaxonomyCategoryValid(String taxonomyCategory, String orgTypeCode);
}
