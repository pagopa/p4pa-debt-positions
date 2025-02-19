package it.gov.pagopa.pu.debtpositions.service.update;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
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
public class UpdateDebtPositionServiceImpl implements UpdateDebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final DebtPositionProcessorService debtPositionProcessorService;
  private final DebtPositionSyncService debtPositionSyncService;

  public UpdateDebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, DebtPositionMapper debtPositionMapper, InstallmentNoPIIRepository installmentNoPIIRepository, PaymentOptionRepository paymentOptionRepository, DebtPositionProcessorService debtPositionProcessorService, DebtPositionSyncService debtPositionSyncService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.debtPositionProcessorService = debtPositionProcessorService;
    this.debtPositionSyncService = debtPositionSyncService;
  }

  @Override
  public String updateDebtPositionByInstallment(DebtPositionDTO debtPositionSynchronizeDTO, Boolean massive, InstallmentDTO installmentDTO, String accessToken) {
    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installmentDTO.getInstallmentId());
    DebtPositionDTO fullDebtPositionDTO = debtPositionMapper.mapToDto(debtPosition);

    //TODO check fields not editable

    updateDebtPosition(fullDebtPositionDTO, debtPositionSynchronizeDTO);

    PaymentOptionDTO paymentOptionSynchronizeDTO = debtPositionSynchronizeDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionId().equals(installmentDTO.getPaymentOptionId()))
      .findFirst()
      .orElseThrow(() -> new NotFoundException(String.format("Payment option related to the id %s was not found", installmentDTO.getPaymentOptionId())));

    InstallmentDTO installmentSynchronizeDTO = paymentOptionSynchronizeDTO.getInstallments().stream()
      .filter(installment -> installment.getInstallmentId().equals(installmentDTO.getInstallmentId()))
      .findFirst()
      .orElseThrow(() -> new NotFoundException(String.format("Installment related to the id %s was not found", installmentDTO.getInstallmentId())));

    fullDebtPositionDTO.getPaymentOptions().stream()
      .filter(po -> po.getPaymentOptionId().equals(installmentDTO.getPaymentOptionId()))
      .findFirst()
      .ifPresent(po -> {
        po.getInstallments().stream()
          .filter(installment -> installment.getInstallmentId().equals(installmentDTO.getInstallmentId()))
          .findFirst()
          .ifPresent(installment -> modifyInstallment(installment, installmentSynchronizeDTO));
        modifyPaymentOption(po, paymentOptionSynchronizeDTO);
      });

    WorkflowCreatedDTO workflowCreatedDTO = debtPositionSyncService.syncDebtPosition(fullDebtPositionDTO, massive, PaymentEventType.DP_UPDATED, accessToken);
    return workflowCreatedDTO.getWorkflowId();
  }

  @Override
  public void modifyInstallment(InstallmentDTO installmentDTO, InstallmentDTO installmentSynchronizeDTO) {
    installmentDTO.setStatus(InstallmentStatus.TO_SYNC);
    installmentDTO.setDueDate(installmentSynchronizeDTO.getDueDate());
    installmentDTO.setAmountCents(installmentSynchronizeDTO.getAmountCents());
    installmentDTO.setRemittanceInformation(installmentSynchronizeDTO.getRemittanceInformation());
    installmentDTO.setBalance(installmentSynchronizeDTO.getBalance());
    installmentDTO.setLegacyPaymentMetadata(installmentSynchronizeDTO.getLegacyPaymentMetadata());
    installmentDTO.setNotificationDate(installmentSynchronizeDTO.getNotificationDate());

    // TODO set also SyncStatus

    installmentNoPIIRepository.update(installmentDTO.getInstallmentId(), InstallmentStatus.TO_SYNC,
      installmentSynchronizeDTO.getDueDate(), installmentSynchronizeDTO.getAmountCents(),
      installmentSynchronizeDTO.getRemittanceInformation(), installmentSynchronizeDTO.getBalance(),
      installmentSynchronizeDTO.getLegacyPaymentMetadata(), installmentSynchronizeDTO.getNotificationDate());
  }

  @Override
  public void modifyPaymentOption(PaymentOptionDTO paymentOptionDTO, PaymentOptionDTO paymentOptionSynchronizeDTO) {
    paymentOptionDTO.setDescription(paymentOptionSynchronizeDTO.getDescription());
    paymentOptionDTO.setStatus(PaymentOptionStatus.TO_SYNC);
    debtPositionProcessorService.updatePaymentOptionAmounts(paymentOptionDTO);
    paymentOptionRepository.update(paymentOptionDTO.getPaymentOptionId(),
      PaymentOptionStatus.TO_SYNC,
      paymentOptionDTO.getTotalAmountCents(),
      paymentOptionSynchronizeDTO.getDescription());
  }

  @Override
  public void updateDebtPosition(DebtPositionDTO debtPositionDTO, DebtPositionDTO debtPositionSynchronizeDTO) {
    debtPositionDTO.setDescription(debtPositionSynchronizeDTO.getDescription());
    debtPositionDTO.setValidityDate(debtPositionSynchronizeDTO.getValidityDate());
    debtPositionDTO.setStatus(DebtPositionStatus.TO_SYNC);
    debtPositionRepository.update(debtPositionDTO.getDebtPositionId(),
      debtPositionSynchronizeDTO.getDescription(),
      debtPositionSynchronizeDTO.getValidityDate());
  }
}
