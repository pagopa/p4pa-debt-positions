package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.create.debtposition.DebtPositionProcessorService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CancellationDebtPositionServiceImpl implements CancellationDebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final DebtPositionSyncService debtPositionSyncService;

  public CancellationDebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionMapper debtPositionMapper, DebtPositionProcessorService debtPositionProcessorService, DebtPositionSyncService debtPositionSyncService) {
    this.debtPositionRepository = debtPositionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionSyncService = debtPositionSyncService;
  }

  @Override
  public String cancelInstallment(InstallmentDTO installmentDTO, String accessToken) {
    //update installment status
    installmentDTO.setSyncStatus(new InstallmentSyncStatus(installmentDTO.getStatus(), InstallmentStatus.CANCELLED));
    installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
    log.info("Updating status cancelled for installment with id {}", installmentDTO.getInstallmentId());
    installmentNoPIIRepository.updateStatus(installmentDTO.getInstallmentId(),
      installmentDTO.getStatus(), installmentDTO.getSyncStatus());

    //find the DP associated
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installmentDTO.getInstallmentId());
    DebtPositionDTO fullDebtPositionDTO = debtPositionMapper.mapToDto(debtPosition);

    debtPositionProcessorService.synchronizeAmountsAndStatus(fullDebtPositionDTO, installmentDTO);

    WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(fullDebtPositionDTO, true, PaymentEventType.DP_UPDATED, accessToken);
    return workflowCreatedDTO.getWorkflowId();
  }
}
