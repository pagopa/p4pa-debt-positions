package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusTransitionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.sync.DebtPositionSyncService;
import it.gov.pagopa.pu.debtpositions.util.Constants;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
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
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final TransferRepository transferRepository;


  public DebtPositionHierarchyStatusAlignerServiceImpl(DebtPositionRepository debtPositionRepository,
                                                       InstallmentNoPIIRepository installmentNoPIIRepository,
                                                       PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService,
                                                       DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService,
                                                       DebtPositionMapper debtPositionMapper,
                                                       DebtPositionSyncService debtPositionSyncService,
                                                       DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
                                                       TransferRepository transferRepository) {
    this.debtPositionRepository = debtPositionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.paymentOptionInnerStatusAlignerService = paymentOptionInnerStatusAlignerService;
    this.debtPositionInnerStatusAlignerService = debtPositionInnerStatusAlignerService;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionSyncService = debtPositionSyncService;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.transferRepository = transferRepository;
  }

  @Transactional
  @Override
  public DebtPositionDTO finalizeSyncStatus(Long debtPositionId, SyncStatusUpdateRequestDTO syncStatusDTO) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByDebtPositionId(debtPositionId);

    if (debtPosition == null) {
      throw new NotFoundException("DEBT_POSITION_NOT_FOUND", String.format("Debt position related to the id %s was not found", debtPositionId));
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
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByTransferId(transferId);

    if (debtPosition == null) {
      throw new NotFoundException("DEBT_POSITION_NOT_FOUND", String.format("Debt position related to the transfer with id %s was not found", transferId));
    }

    String reportedIuds = debtPosition.getPaymentOptions().stream()
      .flatMap(p -> p.getInstallments().stream())
      .filter(i -> i.getTransfers().stream().anyMatch(transfer -> transfer.getTransferId().equals(transferId)))
      .filter(i ->
        switch (i.getStatus()) {
          case InstallmentStatus.REPORTED -> false;
          case InstallmentStatus.PAID -> true;
          default ->
            throw new InvalidStatusTransitionException("INVALID_INSTALLMENT_STATUS", "The installment with id " + i.getInstallmentId() + " is in " + i.getStatus() + " status and cannot be set to reported status");
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

    if (debtPosition.getDebtPositionOrigin().equals(DebtPositionOrigin.SPONTANEOUS_MIXED)) {
      log.debug("Debt position with id {} have SPONTANEOUS_MIXED origin", debtPosition.getDebtPositionId());

      Pair<InstallmentNoPII, Transfer> installmentAndTransfer = debtPosition.getPaymentOptions().stream()
        .flatMap(p -> p.getInstallments().stream())
        .flatMap(i -> i.getTransfers().stream()
          .filter(t -> t.getTransferId().equals(transferId))
          .map(t -> Pair.of(i, t)))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("TRANSFER_NOT_FOUND", String.format("Transfer with id %s was not found in debt position with id %s", transferId, debtPosition.getDebtPositionId())));

      Transfer transfer = transferRepository.findByOrganizationIdAndIuvAndTransferIndex(
        debtPosition.getOrganizationId(),
        installmentAndTransfer.getLeft().getIuv(),
        installmentAndTransfer.getRight().getTransferIndex()
      ).orElseThrow(() -> new NotFoundException("TRANSFER_NOT_FOUND", String.format("Transfer with id %s was not found", transferId)));

      return notifyReportedTransferId(transfer.getTransferId(), transferReportedRequest, accessToken);
    }

    return Pair.of(debtPositionDTO, workflow);
  }

  @Transactional
  @Override
  public Pair<DebtPositionDTO, WorkflowCreatedDTO> checkAndUpdateInstallmentExpiration(Long debtPositionId, String accessToken) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByDebtPositionId(debtPositionId);

    if (debtPosition == null) {
      throw new NotFoundException("DEBT_POSITION_NOT_FOUND", String.format("Debt position related to the id %s was not found", debtPositionId));
    }

    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPosition.getDebtPositionTypeOrgId())
      .orElseThrow(() -> new NotFoundException("DEBT_POSITION_TYPE_ORG_NOT_FOUND", String.format("DebtPositionTypeOrg with id %d was not found", debtPosition.getDebtPositionTypeOrgId())));

    Set<String> expiredIuvs = new HashSet<>();
    String expiredIuds = updateExpiredInstallmentsStatus(debtPosition, i -> expiredIuvs.add(i.getIuv()))
      .collect(Collectors.joining(","));

    DebtPositionDTO debtPositionDTO = alignHierarchyStatusAndRemap(debtPosition);

    PaymentEventType paymentEventType = StringUtils.isNotEmpty(expiredIuds) ? PaymentEventType.DPI_EXPIRED : null;
    WorkflowCreatedDTO workflowCreated = debtPositionSyncService.syncDebtPosition(debtPositionDTO, new WfExecutionParameters(),
      paymentEventType, paymentEventType != null ? "IUD:" + expiredIuds : null, accessToken);

    handleMixedTechDPExpiration(debtPosition, debtPositionTypeOrg, expiredIuvs);

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

  private void handleMixedTechDPExpiration(DebtPosition debtPosition, DebtPositionTypeOrg debtPositionTypeOrg, Set<String> iuvs) {
    if (
      !Constants.MIXED_DP_TYPE_ORG_CODE.equals(debtPositionTypeOrg.getCode())
      || iuvs.isEmpty()
    ) {
      return;
    }


    List<DebtPosition> mixedDebtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndExpiredIuvs(
      debtPosition.getOrganizationId(),
      iuvs.stream().toList(),
      List.of(DebtPositionOrigin.SPONTANEOUS_MIXED)
    );

    String expiredMixedIuds = mixedDebtPositions.stream()
      .map(dp -> {
        List<String> iuds = updateExpiredInstallmentsStatus(dp, null).toList();
        alignHierarchyStatus(debtPosition);
        return iuds;
      })
      .flatMap(List::stream)
      .collect(Collectors.joining(","));

    log.info("expired mixed iuds for debtPosition={}: {}", debtPosition.getDebtPositionId(),  expiredMixedIuds);
  }

  private Stream<String> updateExpiredInstallmentsStatus(DebtPosition debtPosition, Consumer<InstallmentNoPII> action) {
    return debtPosition.getPaymentOptions().stream()
      .flatMap(paymentOption -> paymentOption.getInstallments().stream())
      .filter(i -> i.getStatus().equals(InstallmentStatus.UNPAID) &&
        i.getDueDate() != null && i.getDueDate().isBefore(LocalDate.now()) && i.isSwitchToExpired())
      .map(i -> {
          InstallmentStatus newStatus = InstallmentStatus.EXPIRED;
          i.setStatus(InstallmentStatus.EXPIRED);
          log.info("Updating status {} for installment with id {} related to debt position {} after checking the due date", newStatus, i.getInstallmentId(), debtPosition.getDebtPositionId());
          installmentNoPIIRepository.updateStatus(i.getInstallmentId(), newStatus, null);
          if (action != null) {
            action.accept(i);
          }
          return i.getIud();
        }
      );
  }
}
