package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
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
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final DebtPositionSyncService debtPositionSyncService;

  public CancellationDebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository, InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionMapper debtPositionMapper, DebtPositionProcessorService debtPositionProcessorService, DebtPositionSyncService debtPositionSyncService) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionSyncService = debtPositionSyncService;
  }


  @Override
  public String cancelInstallment(InstallmentDTO installmentDTO, String accessToken) {
    //update installment status
    InstallmentStatus newStatus = InstallmentStatus.TO_SYNC;
    installmentDTO.setStatus(newStatus);
    installmentDTO.setSyncStatus(new InstallmentSyncStatus(installmentDTO.getStatus(), InstallmentStatus.CANCELLED));
    log.info("Updating status {} for installment with id {} in order to cancel it", newStatus, installmentDTO.getInstallmentId());
    installmentNoPIIRepository.updateStatus(installmentDTO.getInstallmentId(), newStatus);

    //find the DP associated
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installmentDTO.getInstallmentId());

    //update amount
    DebtPositionDTO fullDebtPositionDTO = debtPositionMapper.mapToDto(debtPosition);

    fullDebtPositionDTO.getPaymentOptions().stream()
      .filter(paymentOptionDTO -> paymentOptionDTO.getPaymentOptionId().equals(installmentDTO.getPaymentOptionId()))
      .findFirst()
      .ifPresent(paymentOptionDTO -> {
        debtPositionProcessorService.updatePaymentOptionAmounts(paymentOptionDTO);
        paymentOptionRepository.updateTotalAmounts(paymentOptionDTO.getPaymentOptionId(), paymentOptionDTO.getTotalAmountCents());
      });

    WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(fullDebtPositionDTO, true, PaymentEventType.DP_UPDATED, accessToken);
    return workflowCreatedDTO.getWorkflowId();
  }
}
