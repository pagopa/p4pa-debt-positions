package it.gov.pagopa.pu.debtpositions.service.massive.action;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.update.CancellationDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.UpdateDebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class UpdateActionMassiveDebtPositionServiceImpl implements UpdateActionMassiveDebtPositionService {

  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final InstallmentMapper installmentMapper;
  private final UpdateDebtPositionService updateDebtPositionService;
  private final CancellationDebtPositionService cancellationDebtPositionService;

  private static final Set<InstallmentStatus> installmentStatusesValidForUpdate =
    Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);

  public UpdateActionMassiveDebtPositionServiceImpl(InstallmentNoPIIRepository installmentNoPIIRepository, InstallmentMapper installmentMapper, UpdateDebtPositionService updateDebtPositionService, CancellationDebtPositionService cancellationDebtPositionService) {
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.installmentMapper = installmentMapper;
    this.updateDebtPositionService = updateDebtPositionService;
    this.cancellationDebtPositionService = cancellationDebtPositionService;
  }

  @Override
  public String handleUpdate(DebtPositionDTO debtPositionSynchronizeDTO, Boolean massive, String accessToken) {
    InstallmentDTO installment = checkUpdateProcessable(debtPositionSynchronizeDTO);
    return updateDebtPositionService.updateDebtPositionByInstallment(debtPositionSynchronizeDTO, massive, installment, accessToken);
  }

  @Override
  public String handleCancellation(DebtPositionDTO debtPositionSynchronizeDTO, Boolean massive, String accessToken) {
    InstallmentDTO installment = checkUpdateProcessable(debtPositionSynchronizeDTO);
    return cancellationDebtPositionService.cancelInstallment(installment, massive, accessToken);
  }

  public InstallmentDTO checkUpdateProcessable(DebtPositionDTO debtPositionSynchronizeDTO) {
    InstallmentNoPII installment = installmentNoPIIRepository.getByOrganizationIdAndIudAndPaymentOptionIndexAndIuv(
      debtPositionSynchronizeDTO.getOrganizationId(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIud(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getPaymentOptionIndex(),
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getIuv()
    ).orElse(null);

    if (installment == null) {
      throw new ConflictErrorException("The installment cannot be modified because it doesn't exist");
    }

    if (!installmentStatusesValidForUpdate.contains(installment.getStatus())) {
      throw new ConflictErrorException("The installment cannot be modified because is not in an allowed status");
    }

    checkInstallmentInToSyncIfProcessable(installment,
      debtPositionSynchronizeDTO.getPaymentOptions().getFirst().getInstallments().getFirst());
    return installmentMapper.mapToDto(installment);
  }

  private void checkInstallmentInToSyncIfProcessable(InstallmentNoPII installment, InstallmentDTO installmentDTO) {
    if (InstallmentStatus.TO_SYNC.equals(installment.getStatus())) {
      if (!installmentDTO.getIngestionFlowFileId().equals(installment.getIngestionFlowFileId())) {
        throw new ConflictErrorException("The installment cannot be modified because there was an error in the previous synchronization");
      } else if (installment.getSyncStatus() != null && !installmentStatusesValidForUpdate.contains(installment.getSyncStatus().getSyncStatusTo())) {
        throw new ConflictErrorException("The installment cannot be modified because is not in an allowed status");
      }
    }
  }

}
