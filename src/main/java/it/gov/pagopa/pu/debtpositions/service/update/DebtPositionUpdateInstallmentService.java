package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface DebtPositionUpdateInstallmentService {

  /***
   *
   * @param debtPositionDTO the debt position involved
   * @param installments2operate list of {@link InstallmentDTO} to be updated
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the operation
   * @return the {@link DebtPositionDTO} updated and WorkflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> updateInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, Boolean massive, String accessToken, String operatorExternalUserId);

}
