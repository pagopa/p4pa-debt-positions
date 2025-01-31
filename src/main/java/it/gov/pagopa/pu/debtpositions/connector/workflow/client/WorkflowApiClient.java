package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.workflowhub.dto.generated.DebtPositionRequestDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public WorkflowCreatedDTO handleDpSync(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .handleDpSync(debtPositionRequestDTO);
  }

  public WorkflowCreatedDTO alignDpSyncAca(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpSyncAca(debtPositionRequestDTO);
  }

  public WorkflowCreatedDTO alignDpSyncGpdPreload(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpSyncGpdPreload(debtPositionRequestDTO);
  }

  public WorkflowCreatedDTO alignDpSyncAcaGpdPreload(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpSyncAcaGpdPreload(debtPositionRequestDTO);
  }

  public WorkflowCreatedDTO alignDpGPD(DebtPositionRequestDTO debtPositionRequestDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpGPD(debtPositionRequestDTO);
  }

}
