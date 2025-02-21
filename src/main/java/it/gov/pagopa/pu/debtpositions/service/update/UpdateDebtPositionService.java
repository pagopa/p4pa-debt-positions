package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;

import java.util.List;

public interface UpdateDebtPositionService {
    String updateDebtPosition(DebtPositionDTO debtPositionToSyncDTO, List<Long> installmentIds, Boolean massive, String accessToken);
}
