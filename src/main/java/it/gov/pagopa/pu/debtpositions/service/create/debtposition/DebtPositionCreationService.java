package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.tuple.Pair;

public interface DebtPositionCreationService {

  /***
   *
   * @param debtPositionDTO the debt position to be created
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the creation
   * @return the {@link DebtPositionDTO} created and WorkflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId);

  void checkInstallment(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg, InstallmentDTO installmentDTO);
}
