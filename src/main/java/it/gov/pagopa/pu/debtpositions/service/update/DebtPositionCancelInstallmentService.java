package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface DebtPositionCancelInstallmentService {

  Pair<DebtPositionDTO, String> cancelInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, Boolean massive, String accessToken, String operatorExternalUserId);
}
