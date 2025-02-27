package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;

public interface InstallmentSynchronizeService {

  String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive, DebtPositionOrigin origin, String accessToken, String operatorExternalUserId);
}
