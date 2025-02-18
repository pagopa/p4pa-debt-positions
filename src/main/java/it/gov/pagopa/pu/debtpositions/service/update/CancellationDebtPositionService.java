package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;

public interface CancellationDebtPositionService {

  String cancelInstallment(InstallmentDTO installmentDTO, String accessToken);
}
