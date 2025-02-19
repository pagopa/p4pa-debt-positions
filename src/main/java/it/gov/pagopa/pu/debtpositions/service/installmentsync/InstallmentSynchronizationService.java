package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;

public interface InstallmentSynchronizationService {

  String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive, String accessToken, String operatorExternalUserId);
}
