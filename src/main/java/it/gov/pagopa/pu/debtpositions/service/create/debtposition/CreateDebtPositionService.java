package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface CreateDebtPositionService {

  /***
   *
   * @param debtPositionDTO the debt position to be created
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the creation
   * @return the {@link DebtPositionDTO} created and workflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId);

}
