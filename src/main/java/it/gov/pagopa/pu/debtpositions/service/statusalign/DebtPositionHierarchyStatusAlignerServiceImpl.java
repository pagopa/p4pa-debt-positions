package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.IupdSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusTransitionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus.TO_SYNC;

@Service
@Slf4j
public class DebtPositionHierarchyStatusAlignerServiceImpl implements DebtPositionHierarchyStatusAlignerService {

  private final DebtPositionRepository debtPositionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService;
  private final DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService;
  private final DebtPositionMapper debtPositionMapper;


  public DebtPositionHierarchyStatusAlignerServiceImpl(DebtPositionRepository debtPositionRepository,
                                                       InstallmentNoPIIRepository installmentNoPIIRepository, PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService, DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService, DebtPositionMapper debtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.paymentOptionInnerStatusAlignerService = paymentOptionInnerStatusAlignerService;
    this.debtPositionInnerStatusAlignerService = debtPositionInnerStatusAlignerService;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Transactional
  @Override
  public DebtPositionDTO finalizeSyncStatus(Long debtPositionId, Map<String, IupdSyncStatusUpdateDTO> syncStatusDTO) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);

    if (debtPosition == null) {
      throw new NotFoundException("Debt position related to the id requested does not found");
    }

    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().stream()
        .filter(installment -> {
          boolean isToSync = TO_SYNC.equals(installment.getStatus());
          boolean iud2Update = syncStatusDTO.containsKey(installment.getIud());

          if (!iud2Update && isToSync) {
            log.error("Installment with IUD [{}] is TO_SYNC but not present in the input map", installment.getIud());
          } else if (iud2Update && !isToSync) {
            log.error("Installment with IUD [{}] is present in the input map but does not have TO_SYNC status", installment.getIud());
          }

          return isToSync && iud2Update;
        })
        .forEach(installment -> {
          IupdSyncStatusUpdateDTO updateDTO = syncStatusDTO.get(installment.getIud());

          InstallmentStatus newStatus = updateDTO.getNewStatus();
          installment.setStatus(newStatus);
          log.info("Updating status {} and iupdPagopa {} for installment with id {}", newStatus, updateDTO.getIupdPagopa(), installment.getInstallmentId());
          installmentNoPIIRepository.updateStatusAndIupdPagopa(
            installment.getInstallmentId(),
            updateDTO.getIupdPagopa(),
            newStatus
          );
        })
    );

    return updateAllStatus(debtPosition);
  }

  @Override
  public DebtPositionDTO notifyReportedTransferId(Long transferId) {
    DebtPosition debtPosition = debtPositionRepository.findByTransferId(transferId);

    if (debtPosition == null) {
      throw new NotFoundException("Debt position related to the transfer requested does not found");
    }

    debtPosition.getPaymentOptions().stream()
      .flatMap(p -> p.getInstallments().stream())
      .filter(i -> i.getTransfers().stream().anyMatch(transfer -> transfer.getTransferId().equals(transferId)))
      .findFirst()
      .filter(i ->
        switch (i.getStatus()) {
          case InstallmentStatus.REPORTED -> false;
          case InstallmentStatus.PAID -> true;
          default ->
            throw new InvalidStatusTransitionException("The installment is not in the paid status to be set in reported status");
        })
      .ifPresent(installment -> {
        InstallmentStatus newStatus = InstallmentStatus.REPORTED;
        installment.setStatus(newStatus);
        log.info("Updating status {} for installment with id {}", newStatus, installment.getInstallmentId());
        installmentNoPIIRepository.updateStatus(installment.getInstallmentId(), newStatus);
      });

    return updateAllStatus(debtPosition);
  }

  private DebtPositionDTO updateAllStatus(DebtPosition debtPosition) {
    debtPosition.getPaymentOptions().forEach(paymentOptionInnerStatusAlignerService::updatePaymentOptionStatus);
    debtPositionInnerStatusAlignerService.updateDebtPositionStatus(debtPosition);

    return debtPositionMapper.mapToDto(debtPosition);
  }
}
