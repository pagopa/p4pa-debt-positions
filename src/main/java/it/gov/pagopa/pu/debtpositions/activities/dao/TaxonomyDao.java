package it.gov.pagopa.pu.debtpositions.activities.dao;


public interface TaxonomyDao {

    /**
     * Verifies if the specified category exists in the taxonomy.
     *
     * @param category the category name to verify
     * @return {@code true} if the category exists, {@code false} otherwise
     */
    Boolean verifyCategory(String category);
}


