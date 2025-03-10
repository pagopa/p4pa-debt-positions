package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.BaseInstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.apply.InstallmentSynchronizeApplierService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionAddInstallmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class InstallmentSynchronizeInsertService extends BaseInstallmentSynchronizeService {

  private final InstallmentSynchronizeApplierService installmentSynchronizeApplierService;
  private final DebtPositionCreationService debtPositionCreationService;
  private final DebtPositionAddInstallmentService debtPositionAddInstallmentService;

  private static final Set<InstallmentStatus> installmentStatusesValidForInsertion = Set.of(InstallmentStatus.CANCELLED, InstallmentStatus.INVALID);
  private static final Set<PaymentOptionStatus> paymentOptionStatusesValidForInsertion = Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID);
  private static final Set<DebtPositionStatus> debtPositionStatusesValidForInsertion = Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID);

  public InstallmentSynchronizeInsertService(InstallmentSynchronizeApplierService installmentSynchronizeApplierService, DebtPositionCreationService debtPositionCreationService, DebtPositionAddInstallmentService debtPositionAddInstallmentService) {
    this.installmentSynchronizeApplierService = installmentSynchronizeApplierService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionAddInstallmentService = debtPositionAddInstallmentService;
  }

  public String syncInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO storedDebtPosition, Boolean massive, String accessToken, String operatorExternalUserId) {
    Pair<PaymentOptionDTO, InstallmentDTO> result = findInstallment(storedDebtPosition, installmentSynchronizeDTO);
    PaymentOptionDTO storedPaymentOption = result == null ? null: result.getLeft();
    InstallmentDTO storedInstallment = result == null ? null: result.getRight();

    if(isInstallmentAlreadyElaborated(storedInstallment, installmentSynchronizeDTO)){ return null;}
    checkStatus(storedDebtPosition, storedPaymentOption, storedInstallment);

    Pair<DebtPositionDTO, InstallmentDTO> debtPositionApplied = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, storedDebtPosition, storedPaymentOption, storedInstallment, accessToken);

    if(debtPositionApplied.getLeft().getDebtPositionId() == null) {
      return debtPositionCreationService.createDebtPosition(debtPositionApplied.getLeft(), massive, accessToken, operatorExternalUserId).getRight();
    }

    return debtPositionAddInstallmentService.addInstallment(debtPositionApplied.getLeft(), List.of(debtPositionApplied.getRight()), massive, accessToken, operatorExternalUserId).getRight();
  }

  private void checkStatus(DebtPositionDTO storedDebtPosition, PaymentOptionDTO storedPaymentOption, InstallmentDTO storedInstallment) {
    if(storedDebtPosition != null && !debtPositionStatusesValidForInsertion.contains(storedDebtPosition.getStatus())){
      throw new ConflictErrorException(String.format("The installment cannot be created because the debt position with iupd %s is not in an allowed status: %s",
        storedDebtPosition.getIupdOrg(), storedDebtPosition.getStatus()));
    }
    if(storedPaymentOption != null && !paymentOptionStatusesValidForInsertion.contains(storedPaymentOption.getStatus())) {
      throw new ConflictErrorException(String.format("The installment cannot be created because the payment option with index %s is not in an allowed status: %s",
        storedPaymentOption.getPaymentOptionIndex(), storedPaymentOption.getStatus()));
    }
    if (storedInstallment != null && !installmentStatusesValidForInsertion.contains(storedInstallment.getStatus())) {
      throw new ConflictErrorException(String.format("The installment with iud %s cannot be created because it already exists in a not modifiable status: %s",
        storedInstallment.getIud(), storedInstallment.getStatus()));
    }
  }


}
