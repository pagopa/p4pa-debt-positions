package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;

import java.util.List;
import java.util.Set;

/***
 * This interface provides method relating to assessments registry information
 */
public interface AssessmentsRegistryService {

  /**
   * Finds assessments registries based on the provided filters and pagination options.
   *
   * @param organizationId the unique identifier for the organization (can be null to ignore this filter)
   * @param debtPositionTypeOrgCodes a set of debt position type organization codes to filter by (can be null to ignore this filter)
   * @param sectionCode the code of the section to filter by (can be null to ignore this filter)
   * @param sectionDescription the description of the section to filter by (can be null to ignore this filter)
   * @param officeCode the code of the office to filter by (can be null to ignore this filter)
   * @param officeDescription the description of the office to filter by (can be null to ignore this filter)
   * @param assessmentCode the code of the assessment to filter by (can be null to ignore this filter)
   * @param assessmentDescription the description of the assessment to filter by (can be null to ignore this filter)
   * @param operatingYear the year of operation to filter by (can be null to ignore this filter)
   * @param status the status of the assessments registry to filter by (can be null to ignore this filter)
   * @param page the page number for pagination (starting from 0)
   * @param size the size of the page (the number of records to return per page)
   * @param sort a list of properties to sort the results by (can be null to apply default sorting)
   * @param accessToken the access token
   * @return a PagedModelAssessmentsRegistry containing the filtered assessments registries along with pagination information
   */
  PagedModelAssessmentsRegistry findAssessmentsRegistriesByFilters(Long organizationId,
                                                                   Set<String> debtPositionTypeOrgCodes,
                                                                   String sectionCode,
                                                                   String sectionDescription,
                                                                   String officeCode,
                                                                   String officeDescription,
                                                                   String assessmentCode,
                                                                   String assessmentDescription,
                                                                   String operatingYear,
                                                                   AssessmentsRegistryStatus status,
                                                                   Integer page,
                                                                   Integer size,
                                                                   List<String> sort,
                                                                   String accessToken);
}
