package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusTransitionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus.TO_SYNC;

@Service
@Slf4j
public class DebtPositionHierarchyStatusAlignerServiceImpl implements DebtPositionHierarchyStatusAlignerService {

  private final DebtPositionRepository debtPositionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService;
  private final DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionSyncService debtPositionSyncService;



  public DebtPositionHierarchyStatusAlignerServiceImpl(DebtPositionRepository debtPositionRepository,
                                                       InstallmentNoPIIRepository installmentNoPIIRepository, PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService, DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService, DebtPositionMapper debtPositionMapper, DebtPositionSyncService debtPositionSyncService) {
    this.debtPositionRepository = debtPositionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.paymentOptionInnerStatusAlignerService = paymentOptionInnerStatusAlignerService;
    this.debtPositionInnerStatusAlignerService = debtPositionInnerStatusAlignerService;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionSyncService = debtPositionSyncService;
  }

  @Transactional
  @Override
  public DebtPositionDTO finalizeSyncStatus(Long debtPositionId, SyncStatusUpdateRequestDTO syncStatusDTO) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);

    if (debtPosition == null) {
      throw new NotFoundException(String.format("Debt position related to the id %s was not found", debtPositionId));
    }

    Set<String> iupd2update = Stream.concat(
      syncStatusDTO.getIupd2finalize().keySet().stream(),
      syncStatusDTO.getIupdSyncError().keySet().stream()
    ).collect(Collectors.toSet());

    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().stream()
        .filter(installment -> {
          boolean isToSync = TO_SYNC.equals(installment.getStatus());
          boolean iud2Update = iupd2update.contains(installment.getIud());

          if (!iud2Update && isToSync) {
            log.error("Installment with IUD [{}] is TO_SYNC but not present in the input map", installment.getIud());
          } else if (iud2Update && !isToSync) {
            log.error("Installment with IUD [{}] is present in the input map but does not have TO_SYNC status", installment.getIud());
          }

          return isToSync && iud2Update;
        })
        .forEach(installment -> {
          handleIupdStatusUpdate(installment, syncStatusDTO.getIupd2finalize().get(installment.getIud()));
          handleIupdSyncError(installment, syncStatusDTO.getIupdSyncError().get(installment.getIud()));
        })
    );

    return alignHierarchyStatusAndRemap(debtPosition);
  }

  private void handleIupdStatusUpdate(InstallmentNoPII installment, SyncCompleteDTO updateDTO) {
    if(updateDTO != null) {
      InstallmentStatus newStatus = updateDTO.getNewStatus();
      installment.setStatus(newStatus);
      installment.setSyncStatus(null);
      log.info("Updating status {} for installment with id {}", newStatus, installment.getInstallmentId());
      installmentNoPIIRepository.updateStatus(
        installment.getInstallmentId(),
        newStatus,
        null
      );
    }
  }

  private void handleIupdSyncError(InstallmentNoPII installment, SyncErrorDTO iupdSyncErrorDTO) {
    if(iupdSyncErrorDTO != null) {
      String errorDescription = iupdSyncErrorDTO.getErrorDescription();
      installment.getSyncStatus().setSyncError(errorDescription);
      log.info("Setting syncError for installment with id {}: {}", installment.getInstallmentId(), errorDescription);
      installmentNoPIIRepository.updateStatus(
        installment.getInstallmentId(),
        installment.getStatus(),
        installment.getSyncStatus()
      );
    }
  }

  @Transactional
  @Override
  public Pair<DebtPositionDTO, WorkflowCreatedDTO> notifyReportedTransferId(Long transferId, TransferReportedRequest transferReportedRequest, String accessToken) {
    DebtPosition debtPosition = debtPositionRepository.findByTransferId(transferId);

    if (debtPosition == null) {
      throw new NotFoundException(String.format("Debt position related to the transfer with id %s was not found", transferId));
    }

    String reportedIuds = debtPosition.getPaymentOptions().stream()
      .flatMap(p -> p.getInstallments().stream())
      .filter(i -> i.getTransfers().stream().anyMatch(transfer -> transfer.getTransferId().equals(transferId)))
      .filter(i ->
        switch (i.getStatus()) {
          case InstallmentStatus.REPORTED -> false;
          case InstallmentStatus.PAID -> true;
          default ->
            throw new InvalidStatusTransitionException("The installment with id " + i.getInstallmentId() + " is in " + i.getStatus() + " status and cannot be set to reported status");
        })
      .map(installment -> {
        InstallmentStatus newStatus = InstallmentStatus.REPORTED;
        installment.setStatus(newStatus);
        installment.setIuf(transferReportedRequest.getIuf());
        log.info("Updating status {} for installment with id {} after report notification on transfer {}", newStatus, installment.getInstallmentId(), transferId);
        installmentNoPIIRepository.updateStatusAndIuf(installment.getInstallmentId(), newStatus, installment.getIuf());
        return installment.getIud();
      })
      .collect(Collectors.joining(","));

    DebtPositionDTO debtPositionDTO = alignHierarchyStatusAndRemap(debtPosition);
    WorkflowCreatedDTO workflow = null;

    if(StringUtils.isNotEmpty(reportedIuds)){
      workflow = debtPositionSyncService.syncDebtPosition(debtPositionDTO, new WfExecutionParameters(),
        PaymentEventType.DPI_REPORTED, "IUD:" + reportedIuds, accessToken);
    }

    return Pair.of(debtPositionDTO, workflow);
  }

  @Transactional
  @Override
  public Pair<DebtPositionDTO, WorkflowCreatedDTO> checkAndUpdateInstallmentExpiration(Long debtPositionId, String accessToken) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);

    if (debtPosition == null) {
      throw new NotFoundException(String.format("Debt position related to the id %s was not found", debtPositionId));
    }

    String expiredIuds = debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(i -> i.getStatus().equals(InstallmentStatus.UNPAID) &&
        i.getDueDate() != null && i.getDueDate().isBefore(LocalDate.now()))
      .map(i -> {
          InstallmentStatus newStatus = InstallmentStatus.EXPIRED;
          i.setStatus(InstallmentStatus.EXPIRED);
          log.info("Updating status {} for installment with id {} related to debt position {} after checking the due date", newStatus, i.getInstallmentId(), debtPositionId);
          installmentNoPIIRepository.updateStatus(i.getInstallmentId(), newStatus, null);
          return i.getIud();
        }
      ).collect(Collectors.joining(","));

    DebtPositionDTO debtPositionDTO = alignHierarchyStatusAndRemap(debtPosition);

    PaymentEventType paymentEventType = StringUtils.isNotEmpty(expiredIuds) ? PaymentEventType.DPI_EXPIRED : null;
    WorkflowCreatedDTO workflowCreated = debtPositionSyncService.syncDebtPosition(debtPositionDTO, new WfExecutionParameters(),
      paymentEventType, paymentEventType != null ? "IUD:" + expiredIuds : null, accessToken);

    return Pair.of(debtPositionDTO, workflowCreated);
  }

  @Override
  public void alignHierarchyStatus(BaseDebtPosition debtPosition) {
    debtPosition.getPaymentOptions().forEach(paymentOptionInnerStatusAlignerService::updatePaymentOptionStatus);
    debtPositionInnerStatusAlignerService.updateDebtPositionStatus(debtPosition);
  }

  /**
   * Call only if you have to resolve PII
   */
  protected DebtPositionDTO alignHierarchyStatusAndRemap(DebtPosition debtPosition) {
    alignHierarchyStatus(debtPosition);
    return debtPositionMapper.mapToDto(debtPosition);
  }
}
