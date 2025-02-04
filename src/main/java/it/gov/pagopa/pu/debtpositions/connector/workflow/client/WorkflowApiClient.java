package it.gov.pagopa.pu.debtpositions.connector.workflow.client;

import it.gov.pagopa.pu.debtpositions.connector.workflow.config.WorkflowApisHolder;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.springframework.stereotype.Service;

@Service
public class WorkflowApiClient {

  private final WorkflowApisHolder workflowApisHolder;

  public WorkflowApiClient(WorkflowApisHolder workflowApisHolder) {
    this.workflowApisHolder = workflowApisHolder;
  }

  public WorkflowCreatedDTO handleDpSync(DebtPositionDTO debtPositionDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .handleDpSync(debtPositionDTO);
  }

  public WorkflowCreatedDTO alignDpSyncAca(DebtPositionDTO debtPositionDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpSyncAca(debtPositionDTO);
  }

  public WorkflowCreatedDTO alignDpSyncGpdPreload(DebtPositionDTO debtPositionDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpSyncGpdPreload(debtPositionDTO);
  }

  public WorkflowCreatedDTO alignDpSyncAcaGpdPreload(DebtPositionDTO debtPositionDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpSyncAcaGpdPreload(debtPositionDTO);
  }

  public WorkflowCreatedDTO alignDpGPD(DebtPositionDTO debtPositionDTO, String accessToken) {
    return workflowApisHolder.getDebtPositionApi(accessToken)
      .alignDpGPD(debtPositionDTO);
  }

}
