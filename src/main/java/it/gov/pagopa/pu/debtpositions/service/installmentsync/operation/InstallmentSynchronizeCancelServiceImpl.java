package it.gov.pagopa.pu.debtpositions.service.installmentsync.operation;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.BaseInstallmentSynchronizeService;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionCancelInstallmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class InstallmentSynchronizeCancelServiceImpl {

  private final DebtPositionCancelInstallmentService debtPositionCancelInstallmentService;
  private final BaseInstallmentSynchronizeService baseInstallmentSynchronizeService;

  private static final Set<InstallmentStatus> installmentStatusesValidForCancel =
    Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);

  public InstallmentSynchronizeCancelServiceImpl(DebtPositionCancelInstallmentService debtPositionCancelInstallmentService, BaseInstallmentSynchronizeService baseInstallmentSynchronizeService) {
    this.debtPositionCancelInstallmentService = debtPositionCancelInstallmentService;
    this.baseInstallmentSynchronizeService = baseInstallmentSynchronizeService;
  }

  public String syncInstallment(InstallmentSynchronizeDTO installmentSynchronizeDTO, DebtPositionDTO debtPositionDTO,
                                Boolean massive, String accessToken, String operatorExternalUserId) {
    if (debtPositionDTO == null) {
      throw new NotFoundException(String.format("The debt position related to iupd %s was not found", installmentSynchronizeDTO.getIupdOrg()));
    }

    InstallmentDTO installmentDTO = baseInstallmentSynchronizeService.findInstallment(debtPositionDTO, installmentSynchronizeDTO.getIud(), installmentSynchronizeDTO.getPaymentOptionIndex());

    validateStatus(installmentDTO, installmentSynchronizeDTO);

    return debtPositionCancelInstallmentService.cancelInstallment(debtPositionDTO, List.of(installmentDTO), massive, accessToken, operatorExternalUserId).getRight();
  }

  private void validateStatus(InstallmentDTO installmentDTO, InstallmentSynchronizeDTO installmentSynchronizeDTO) {
    if (InstallmentStatus.TO_SYNC.equals(installmentDTO.getStatus())) {
      if (!installmentSynchronizeDTO.getIngestionFlowFileId().equals(installmentDTO.getIngestionFlowFileId())) {
        throw new ConflictErrorException(String.format("The installment with %s cannot be cancelled because there was an error in the previous synchronization",installmentSynchronizeDTO.getIud()));
      } else if (installmentDTO.getSyncStatus() != null && !installmentStatusesValidForCancel.contains(installmentDTO.getSyncStatus().getSyncStatusTo())) {
        throw new ConflictErrorException(String.format("The installment with %s cannot be cancelled because is not in an allowed status to: %s",
          installmentSynchronizeDTO.getIud(), installmentDTO.getSyncStatus().getSyncStatusTo()));
      }
    } else if (!installmentStatusesValidForCancel.contains(installmentDTO.getStatus())) {
      throw new ConflictErrorException(String.format("The installment with iud %s cannot be cancelled because is not in an allowed status: %s",
        installmentSynchronizeDTO.getIud(), installmentDTO.getStatus()));
    }
  }
}
