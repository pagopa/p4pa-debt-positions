package it.gov.pagopa.pu.debtpositions.service.massive;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;

public interface DebtPositionMassiveService {

  String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, String accessToken, String operatorExternalUserId);
}
