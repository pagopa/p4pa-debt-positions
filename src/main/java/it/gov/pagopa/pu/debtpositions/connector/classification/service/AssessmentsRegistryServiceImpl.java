package it.gov.pagopa.pu.debtpositions.connector.classification.service;

import it.gov.pagopa.pu.classification.dto.generated.AssessmentsRegistryStatus;
import it.gov.pagopa.pu.classification.dto.generated.PagedModelAssessmentsRegistry;
import it.gov.pagopa.pu.debtpositions.connector.classification.client.AssessmentsRegistryClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AssessmentsRegistryServiceImpl implements AssessmentsRegistryService {

  private final AssessmentsRegistryClient assessmentsRegistryClient;

  public AssessmentsRegistryServiceImpl(AssessmentsRegistryClient assessmentsRegistryClient) {
    this.assessmentsRegistryClient = assessmentsRegistryClient;
  }

  @Override
  public PagedModelAssessmentsRegistry findAssessmentsRegistriesByFilters(Long organizationId, Set<String> debtPositionTypeOrgCodes, String sectionCode, String sectionDescription, String officeCode, String officeDescription, String assessmentCode, String assessmentDescription, String operatingYear, AssessmentsRegistryStatus status, Integer page, Integer size, List<String> sort, String accessToken) {
    return assessmentsRegistryClient.findAssessmentsRegistriesByFilters(organizationId, debtPositionTypeOrgCodes, sectionCode, sectionDescription, officeCode, officeDescription, assessmentCode, assessmentDescription, operatingYear, status, page, size, sort, accessToken);
  }
}
