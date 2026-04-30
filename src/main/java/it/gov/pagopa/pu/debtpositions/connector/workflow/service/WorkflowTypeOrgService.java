package it.gov.pagopa.pu.debtpositions.connector.workflow.service;

import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowTypeOrg;
import java.util.Optional;

public interface WorkflowTypeOrgService {
  Optional<WorkflowTypeOrg> getById(String id, String accessToken);
}
