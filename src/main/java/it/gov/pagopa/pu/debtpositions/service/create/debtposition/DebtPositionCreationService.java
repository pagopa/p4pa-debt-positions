package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.apache.commons.lang3.tuple.Pair;

public interface DebtPositionCreationService {

  Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId);

  void checkInstallment(DebtPositionDTO debtPositionDTO, Organization org, DebtPositionTypeOrg debtPositionTypeOrg, InstallmentDTO installmentDTO);
}
