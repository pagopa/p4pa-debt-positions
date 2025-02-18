package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface CreateDebtPositionService {

  Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId);

  Pair<DebtPositionDTO, String> createInstallment(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, PaymentOptionDTO paymentOptionDTO, InstallmentDTO installmentDTO);

  Pair<DebtPositionDTO, String> createPaymentOption(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, PaymentOptionDTO paymentOptionDTO);

}
