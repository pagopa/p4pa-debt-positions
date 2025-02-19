package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;

public interface UpdateDebtPositionService {
  String updateDebtPositionByInstallment(DebtPositionDTO debtPositionSynchronizeDTO, Boolean massive, InstallmentDTO installmentDTO, String accessToken);

  void modifyInstallment(InstallmentDTO installmentDTO, InstallmentDTO installmentSynchronizeDTO);

  void modifyPaymentOption(PaymentOptionDTO paymentOptionDTO, PaymentOptionDTO paymentOptionSynchronizeDTO);

  void updateDebtPosition(DebtPositionDTO debtPositionDTO, DebtPositionDTO debtPositionSynchronizeDTO);
}
