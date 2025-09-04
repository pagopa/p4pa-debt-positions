package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class WorkflowTypeOrgApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowTypeOrgApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public WorkflowTypeOrg findById(String id, String accessToken) {
    try {
      return workflowApisHolder.getWorkflowTypeOrgEntityControllerApi(
          accessToken)
        .crudGetWorkflowtypeorg(id);
    } catch (HttpClientErrorException.NotFound e) {
      log.info("Cannot find WorkflowTypeOrg with id {}", id);
      return null;
    }
  }
}
