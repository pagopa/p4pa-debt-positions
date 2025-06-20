package it.gov.pagopa.pu.debtpositions.connector.classification.client;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;
import it.gov.pagopa.pu.debtpositions.connector.classification.config.ClassificationApisHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AssessmentsRegistryClient {

  private final ClassificationApisHolder classificationApisHolder;

  public AssessmentsRegistryClient(ClassificationApisHolder classificationApisHolder) {
    this.classificationApisHolder = classificationApisHolder;
  }

  public PagedModelAssessmentsRegistry findAssessmentsRegistriesByFilters(Long organizationId, Set<String> debtPositionTypeOrgCodes, String sectionCode, String sectionDescription, String officeCode, String officeDescription, String assessmentCode, String assessmentDescription, String operatingYear, AssessmentsRegistryStatus status, Integer page, Integer size, List<String> sort, String accessToken) {
    return classificationApisHolder.getAssessmentsRegistrySearchControllerApi(accessToken).crudAssessmentsRegistriesFindAssessmentsRegistriesByFilters(organizationId, debtPositionTypeOrgCodes, sectionCode, sectionDescription, officeCode, officeDescription, assessmentCode, assessmentDescription, operatingYear, status, page, size, sort);
  }
}
