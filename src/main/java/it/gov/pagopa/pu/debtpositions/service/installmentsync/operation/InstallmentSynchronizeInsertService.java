package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionCreationService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.BaseInstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.apply.InstallmentSynchronizeApplierService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionAddInstallmentService;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
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
  private static final Set<PaymentOptionStatus> paymentOptionStatusesValidForInsertion = Set.of(PaymentOptionStatus.UNPAID, PaymentOptionStatus.EXPIRED, PaymentOptionStatus.PARTIALLY_PAID, PaymentOptionStatus.TO_SYNC, PaymentOptionStatus.DRAFT);
  private static final Set<DebtPositionStatus> debtPositionStatusesValidForInsertion = Set.of(DebtPositionStatus.UNPAID, DebtPositionStatus.EXPIRED, DebtPositionStatus.PARTIALLY_PAID, DebtPositionStatus.TO_SYNC, DebtPositionStatus.DRAFT);

  public InstallmentSynchronizeInsertService(InstallmentSynchronizeApplierService installmentSynchronizeApplierService, DebtPositionCreationService debtPositionCreationService, DebtPositionAddInstallmentService debtPositionAddInstallmentService) {
    this.installmentSynchronizeApplierService = installmentSynchronizeApplierService;
    this.debtPositionCreationService = debtPositionCreationService;
    this.debtPositionAddInstallmentService = debtPositionAddInstallmentService;
  }

  public WorkflowCreatedDTO syncInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO storedDebtPosition, WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    Pair<PaymentOptionDTO, InstallmentDTO> result = findInstallment(storedDebtPosition, installmentSynchronizeDTO);
    PaymentOptionDTO storedPaymentOption = result == null ? null : result.getLeft();
    InstallmentDTO storedInstallment = result == null ? null : result.getRight();

    if (isInstallmentAlreadyElaborated(storedInstallment, installmentSynchronizeDTO)) {
      return null;
    }
    checkStatus(storedDebtPosition, storedPaymentOption, storedInstallment, installmentSynchronizeDTO.getIngestionFlowFileId());

    Pair<DebtPositionDTO, InstallmentDTO> debtPositionApplied = installmentSynchronizeApplierService.apply(installmentSynchronizeDTO, storedDebtPosition, storedPaymentOption, storedInstallment, accessToken);

    if (debtPositionApplied.getLeft().getDebtPositionId() == null) {
      return debtPositionCreationService.createDebtPosition(debtPositionApplied.getLeft(), wfExecutionParameters, accessToken, operatorExternalUserId);
    }

    return debtPositionAddInstallmentService.addInstallment(debtPositionApplied.getLeft(), List.of(debtPositionApplied.getRight()), wfExecutionParameters, accessToken, operatorExternalUserId);
  }

  private void checkStatus(DebtPositionDTO storedDebtPosition, PaymentOptionDTO storedPaymentOption, InstallmentDTO storedInstallment, Long ingestionFlowFileId) {
    if (storedDebtPosition != null) {
      if (DebtPositionStatus.TO_SYNC.equals(storedDebtPosition.getStatus()) && !isDPToSyncAllowed(storedDebtPosition, ingestionFlowFileId)) {
        throw new ConflictErrorException(String.format("[INVALID_DEBT_POSITION_STATUS] The installment cannot be created because the debt position with iupd %s is in TO_SYNC status for a previous synchronization",
          storedDebtPosition.getIupdOrg()));
      } else if (!debtPositionStatusesValidForInsertion.contains(storedDebtPosition.getStatus())) {
        throw new ConflictErrorException(String.format("[INVALID_DEBT_POSITION_STATUS] The installment cannot be created because the debt position with iupd %s is not in an allowed status: %s",
          storedDebtPosition.getIupdOrg(), storedDebtPosition.getStatus()));
      }
    }
    if (storedPaymentOption != null) {
      if (PaymentOptionStatus.TO_SYNC.equals(storedPaymentOption.getStatus()) && !isPOToSyncAllowed(storedPaymentOption, ingestionFlowFileId)) {
        throw new ConflictErrorException(String.format("[INVALID_PAYMENT_OPTION_STATUS] The installment cannot be created because the payment option with index %s is in TO_SYNC status for a previous synchronization",
          storedPaymentOption.getPaymentOptionIndex()));
      } else if (!paymentOptionStatusesValidForInsertion.contains(storedPaymentOption.getStatus())) {
        throw new ConflictErrorException(String.format("[INVALID_PAYMENT_OPTION_STATUS] The installment cannot be created because the payment option with index %s is not in an allowed status: %s",
          storedPaymentOption.getPaymentOptionIndex(), storedPaymentOption.getStatus()));
      }
    }
    if (storedInstallment != null && !installmentStatusesValidForInsertion.contains(storedInstallment.getStatus())) {
      throw new ConflictErrorException(String.format("[INVALID_INSTALLMENT_STATUS] The installment with iud %s cannot be created because it already exists in a not modifiable status: %s",
        storedInstallment.getIud(), storedInstallment.getStatus()));
    }
  }

  private boolean isDPToSyncAllowed(DebtPositionDTO storedDebtPosition, Long ingestionFlowFileId) {
    return storedDebtPosition.getPaymentOptions().stream().allMatch(
      paymentOptionDTO -> paymentOptionDTO.getInstallments()
        .stream().anyMatch(installmentDTO -> installmentDTO.getIngestionFlowFileId().equals(ingestionFlowFileId))
    );
  }

  private boolean isPOToSyncAllowed(PaymentOptionDTO storedPaymentOption, Long ingestionFlowFileId) {
    return storedPaymentOption.getInstallments()
      .stream().anyMatch(installmentDTO -> installmentDTO.getIngestionFlowFileId().equals(ingestionFlowFileId));
  }

}
