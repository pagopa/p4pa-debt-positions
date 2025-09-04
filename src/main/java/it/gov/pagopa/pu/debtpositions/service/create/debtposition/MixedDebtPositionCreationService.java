package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface MixedDebtPositionCreationService {
  Pair<WorkflowCreatedDTO, DebtPositionDTO> createMixedDebtPosition(MixedDebtPositionDTO mixedDebtPositionDTO, String accessToken, String operatorExternalUserId);
}
