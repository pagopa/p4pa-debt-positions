package it.gov.pagopa.pu.debtpositions.service.massive.action;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.update.CancellationDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.ModificationDebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class UpdateActionMassiveDebtPositionServiceImpl implements UpdateActionMassiveDebtPositionService {

  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final InstallmentMapper installmentMapper;
  private final ModificationDebtPositionService modificationDebtPositionService;
  private final CancellationDebtPositionService cancellationDebtPositionService;

  private static final Set<InstallmentStatus> installmentStatusesValidForModification = Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);

  public UpdateActionMassiveDebtPositionServiceImpl(InstallmentNoPIIRepository installmentNoPIIRepository, InstallmentMapper installmentMapper, ModificationDebtPositionService modificationDebtPositionService, CancellationDebtPositionService cancellationDebtPositionService) {
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.installmentMapper = installmentMapper;
    this.modificationDebtPositionService = modificationDebtPositionService;
    this.cancellationDebtPositionService = cancellationDebtPositionService;
  }

  @Override
  public String handleModification(DebtPositionDTO debtPositionSynchronizeDTO, String accessToken) {
    InstallmentDTO installment = checkUpdateProcessable(debtPositionSynchronizeDTO);
    return modificationDebtPositionService.modifyDebtPosition(debtPositionSynchronizeDTO, installment);
  }

  @Override
  public String handleCancellation(DebtPositionDTO debtPositionSynchronizeDTO, String accessToken) {
    InstallmentDTO installment = checkUpdateProcessable(debtPositionSynchronizeDTO);
    return cancellationDebtPositionService.cancelInstallment(installment, accessToken);
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

    if (!installmentStatusesValidForModification.contains(installment.getStatus())) {
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
      } else if (installment.getSyncStatus() != null && !installmentStatusesValidForModification.contains(installment.getSyncStatus().getSyncStatusTo())) {
        throw new ConflictErrorException("The installment cannot be modified because is not in an allowed status");
      }
    }
  }

}
