package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface DebtPositionCancelInstallmentService {

  /***
   *
   * @param debtPositionDTO the debt position involved
   * @param installments2operate list of {@link InstallmentDTO} to be cancelled
   * @param wfExecutionParameters wf execution parameters
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the operation
   * @return the {@link DebtPositionDTO} updated and WorkflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> cancelInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId);
}
