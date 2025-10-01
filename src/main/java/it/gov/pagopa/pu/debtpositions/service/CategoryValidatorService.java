package it.gov.pagopa.pu.debtpositions.service;

public interface CategoryValidatorService {
  boolean isTaxonomyCodeValid(String taxonomyCode);
  boolean isCategoryValid(String category);
}
