package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

import java.util.List;

public interface DebtPositionCancelInstallmentService {

  String cancelInstallment(DebtPositionDTO debtPositionDTO, List<InstallmentDTO> installments2operate, DebtPositionOrigin debtPositionOrigin, Boolean massive, String accessToken, String operatorExternalUserId);
}
