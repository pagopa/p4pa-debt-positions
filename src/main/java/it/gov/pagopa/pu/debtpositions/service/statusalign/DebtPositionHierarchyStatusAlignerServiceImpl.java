package it.gov.pagopa.pu.debtpositions.service.statusalign;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidStatusException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.statusalign.debtposition.DebtPositionInnerStatusAlignerService;
import it.gov.pagopa.pu.debtpositions.service.statusalign.paymentoption.PaymentOptionInnerStatusAlignerService;
import jakarta.transaction.Transactional;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus.TO_SYNC;

public class DebtPositionHierarchyStatusAlignerServiceImpl implements DebtPositionHierarchyStatusAlignerService {

  private final DebtPositionRepository debtPositionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService;
  private final DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService;


  public DebtPositionHierarchyStatusAlignerServiceImpl(DebtPositionRepository debtPositionRepository,
                                                       InstallmentNoPIIRepository installmentNoPIIRepository, PaymentOptionInnerStatusAlignerService paymentOptionInnerStatusAlignerService, DebtPositionInnerStatusAlignerService debtPositionInnerStatusAlignerService) {
    this.debtPositionRepository = debtPositionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.paymentOptionInnerStatusAlignerService = paymentOptionInnerStatusAlignerService;
    this.debtPositionInnerStatusAlignerService = debtPositionInnerStatusAlignerService;
  }

  @Transactional
  @Override
  public void finalizeSyncStatus(Long debtPositionId, Map<String, IudSyncStatusUpdateDTO> syncStatusDTO) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);

    debtPosition.getPaymentOptions().forEach(paymentOption ->
      paymentOption.getInstallments().stream()
        .filter(installment -> TO_SYNC.equals(installment.getStatus()))
        .filter(installment -> syncStatusDTO.containsKey(installment.getIud()))
        .forEach(installment -> {
          IudSyncStatusUpdateDTO updateDTO = syncStatusDTO.get(installment.getIud());

          try {
            InstallmentStatus newStatus = InstallmentStatus.valueOf(updateDTO.getNewStatus().toUpperCase());
            installment.setStatus(newStatus);
            installmentNoPIIRepository.updateStatusAndIupdPagopa(
              installment.getInstallmentId(),
              updateDTO.getIupdPagopa(),
              newStatus
            );
          } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("Invalid status: " + updateDTO.getNewStatus());
          }
        })
    );

    debtPosition.getPaymentOptions().forEach(paymentOptionInnerStatusAlignerService::updatePaymentOptionStatus);
    debtPositionInnerStatusAlignerService.updateDebtPositionStatus(debtPosition);
  }
}
