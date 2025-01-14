package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.enums.DebtPositionStatus.TO_SYNC;

public class DebtPositionServiceImpl implements DebtPositionService{

  private final DebtPositionRepository debtPositionRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository, InstallmentNoPIIRepository installmentNoPIIRepository, DebtPositionMapper debtPositionMapper) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  public DebtPositionDTO finalizeSyncStatus(Long debtPositionId, Map<String, IudSyncStatusUpdateDTO> syncStatusDTO) {
    DebtPosition debtPosition = debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId);

    debtPosition.getPaymentOptions().stream()
      .filter(paymentOption -> {
        boolean anyUpdated = paymentOption.getInstallments().stream()
          .filter(installment -> TO_SYNC.name().equals(installment.getStatus()))
          .filter(installment -> syncStatusDTO.containsKey(installment.getIud()))
          .filter(installment -> installment.getIupdPagopa().equals(syncStatusDTO.get(installment.getIud()).getIupdPagopa()))
          .map(installment -> {
            IudSyncStatusUpdateDTO updateDTO = syncStatusDTO.get(installment.getIud());
            installment.setStatus(updateDTO.getNewStatus());
            installmentNoPIIRepository.updateStatus(
              installment.getInstallmentId(),
              updateDTO.getNewStatus()
            );
            return true;
          })
          .findAny()
          .orElse(false);

        if (anyUpdated) {
          updatePaymentOptionStatus(paymentOption);
        }
        return anyUpdated;
      })
      .findAny()
      .ifPresent(op -> updateDebtPositionStatus(debtPosition));

    return debtPositionMapper.map(debtPosition);
  }

  private void updatePaymentOptionStatus(PaymentOption paymentOption) {
    paymentOption.getInstallments().stream()
      .map(InstallmentNoPII::getStatus)
      .distinct()
      .reduce((first, second) -> null)
      .ifPresent(unifiedStatus -> {
        paymentOption.setStatus(unifiedStatus);
        paymentOptionRepository.updateStatus(paymentOption.getPaymentOptionId(), unifiedStatus);
      });
  }

  private void updateDebtPositionStatus(DebtPosition debtPosition) {
    debtPosition.getPaymentOptions().stream()
      .map(PaymentOption::getStatus)
      .distinct()
      .reduce((first, second) -> null)
      .ifPresent(unifiedStatus -> {
        debtPosition.setStatus(unifiedStatus);
        debtPositionRepository.updateStatus(debtPosition.getDebtPositionId(), unifiedStatus);
      });
  }
}
