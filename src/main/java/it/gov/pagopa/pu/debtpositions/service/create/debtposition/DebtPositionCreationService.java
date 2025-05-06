package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;

public interface DebtPositionCreationService {

  /***
   *
   * @param debtPositionDTO the debt position to be created
   * @param wfExecutionParameters wf execution parameters
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the creation
   * @return the WorkflowId of debt position synchronization
   */
  WorkflowCreatedDTO createDebtPosition(DebtPositionDTO debtPositionDTO, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);

  void checkInstallment(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg, InstallmentDTO installmentDTO);
}
