package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.debtpositions.connector.workflow.client.WorkflowTypeOrgApiClient;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTypeOrgServiceImpl implements WorkflowTypeOrgService {

  private final WorkflowTypeOrgApiClient workflowTypeOrgApiClient;

  public WorkflowTypeOrgServiceImpl(
    WorkflowTypeOrgApiClient workflowTypeOrgApiClient) {
    this.workflowTypeOrgApiClient = workflowTypeOrgApiClient;
  }

  @Override
  public Optional<WorkflowTypeOrg> getById(String id, String accessToken) {
    return Optional.ofNullable(workflowTypeOrgApiClient.findById(id, accessToken));
  }
}
