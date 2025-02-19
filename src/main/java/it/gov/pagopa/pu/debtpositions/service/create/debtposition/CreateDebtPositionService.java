package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import org.apache.commons.lang3.tuple.Pair;

public interface CreateDebtPositionService {

  /***
   *
   * @param debtPositionDTO the debt position to be created
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param operatorExternalUserId the operator who requested the creation
   * @return the {@link DebtPositionDTO} created and workflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken, String operatorExternalUserId);

  /***
   *
   * @param debtPositionDTO the debt position to be updated with new installment
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param installmentDTO the installment to be created
   * @return the {@link DebtPositionDTO} updated with new installment and workflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> createInstallment(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken,
                                                  InstallmentDTO installmentDTO);

  /***
   *
   * @param debtPositionDTO the debt position to be updated with new payment option
   * @param massive indicates that the operation is massive or single
   * @param accessToken the access token
   * @param paymentOptionDTO the payment option to be created
   * @return the {@link DebtPositionDTO} updated with new payment option and workflowId of debt position synchronization
   */
  Pair<DebtPositionDTO, String> createPaymentOption(DebtPositionDTO debtPositionDTO, Boolean massive, String accessToken,
                                                    PaymentOptionDTO paymentOptionDTO);
}
