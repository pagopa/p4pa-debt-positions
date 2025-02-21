package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.service.update.CancellationDebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.update.UpdateDebtPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UpdateInstallmentServiceImpl {

  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionRepository debtPositionRepository;
  private final TransferRepository transferRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final UpdateDebtPositionService updateDebtPositionService;
  private final CancellationDebtPositionService cancellationDebtPositionService;

  private static final Set<InstallmentStatus> installmentStatusesValidForUpdate =
    Set.of(InstallmentStatus.UNPAID, InstallmentStatus.EXPIRED, InstallmentStatus.DRAFT);

  public UpdateInstallmentServiceImpl(InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionRepository debtPositionRepository, TransferRepository transferRepository, DebtPositionMapper debtPositionMapper, UpdateDebtPositionService updateDebtPositionService, CancellationDebtPositionService cancellationDebtPositionService) {
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionRepository = debtPositionRepository;
    this.transferRepository = transferRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.updateDebtPositionService = updateDebtPositionService;
    this.cancellationDebtPositionService = cancellationDebtPositionService;
  }

  public String handleUpdate(DebtPositionDTO debtPositionToSyncDTO, Boolean massive, String accessToken) {
    InstallmentNoPII installment = retrieveAndCheckInstallment(debtPositionToSyncDTO);
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.getInstallmentId());

    if (debtPosition == null) {
      throw new ConflictErrorException("The debt position not found with installment with id: " + installment.getInstallmentId());
    }

    mapIdsToDTO(debtPositionToSyncDTO, debtPosition.getDebtPositionId(), installment);

    return updateDebtPositionService.updateDebtPosition(debtPositionToSyncDTO, List.of(installment.getInstallmentId()), massive, accessToken);
  }

  public String handleCancellation(DebtPositionDTO debtPositionToSyncDTO, Boolean massive, String accessToken) {
    InstallmentNoPII installment = retrieveAndCheckInstallment(debtPositionToSyncDTO);
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.getInstallmentId());
    if (debtPosition == null) {
      throw new ConflictErrorException("The debt position not found with installment with id: " + installment.getInstallmentId());
    }
    DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);

    return cancellationDebtPositionService.cancelInstallment(debtPositionDTO, List.of(installment.getInstallmentId()), massive, accessToken);
  }

  public void mapIdsToDTO(DebtPositionDTO debtPositionToSyncDTO, Long debtPositionId, InstallmentNoPII installment){
    debtPositionToSyncDTO.setDebtPositionId(debtPositionId);
    debtPositionToSyncDTO.getPaymentOptions().getFirst().setPaymentOptionId(installment.getPaymentOptionId());
    debtPositionToSyncDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setInstallmentId(installment.getInstallmentId());

    List<Transfer> transfers = transferRepository.findByInstallmentId(installment.getInstallmentId());

    Map<Integer, Long> indexToIdMap = transfers.stream()
      .collect(Collectors.toMap(
        Transfer::getTransferIndex,
        Transfer::getTransferId
      ));

    debtPositionToSyncDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers()
      .forEach(transferDTO -> {
        Long matchingId = indexToIdMap.get(transferDTO.getTransferIndex());
        transferDTO.setTransferId(matchingId);
      });
  }

  public InstallmentNoPII retrieveAndCheckInstallment(DebtPositionDTO debtPositionSynchronizeDTO) {
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

    return installment;
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
