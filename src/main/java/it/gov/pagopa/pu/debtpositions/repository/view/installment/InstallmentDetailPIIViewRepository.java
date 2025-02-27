package it.gov.pagopa.pu.debtpositions.repository.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;

public interface InstallmentDetailPIIViewRepository {
  InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId);
}
